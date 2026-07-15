# EventHub — High-Concurrency Event Ticketing Platform

## Overview

EventHub is a scalable event-ticketing backend: organizers create and manage events; customers search, hold seats, and purchase tickets. The project exists to solve the *hard* backend problems a real ticketing system faces — concurrent seat booking, fair admission under flash-sale load, reliable async processing, distributed caching, and search — not to be another CRUD app.

Design priorities, in order: **correctness under concurrency → scalability → fault tolerance → developer ergonomics**. AI is a thin assist layer on top of a fully working backend, never a core dependency.

---

## Architecture

Layered, modular monolith (one deployable, clean package seams so it can be split into services later).

```
                        ┌─────────────────────────────────────────┐
   Client ──HTTP──▶     │  Controller  (REST, validation, OpenAPI)  │
                        ├─────────────────────────────────────────┤
                        │  Service     (business logic, @Transactional)
                        ├─────────────────────────────────────────┤
                        │  Repository  (Spring Data JPA / ES / Redis)
                        └───────┬───────────┬──────────┬───────────┘
                                │           │          │
                  ┌─────────────▼──┐  ┌─────▼─────┐  ┌─▼──────────┐
                  │  PostgreSQL    │  │  Redis    │  │ Elasticsearch
                  │ (system of     │  │ holds/    │  │  (search)  │
                  │  record)       │  │ queue/    │  └────────────┘
                  └───────┬────────┘  │ cache/RL  │
                          │           └───────────┘
                  outbox_events poll
                          │
                  ┌───────▼────────┐        consumers
                  │   RabbitMQ     │──▶ ticket+QR (MinIO), email,
                  │   (fan-out)    │    invoice, analytics, rec-score
                  └────────────────┘
```

**Write path is Postgres-first.** Every state change commits to Postgres in one transaction (including an `outbox_events` row). A relay forwards outbox rows to RabbitMQ; consumers do the slow/side-effect work async. Redis holds only ephemeral state (seat holds, waiting-room queue, rate-limit buckets, cache). Elasticsearch is a derived read model rebuilt from Postgres events.

---

## Technology → Use-Case Map

This is *where each tool is actually used* and *why that tool*. No technology is in the stack without a concrete job.

| Technology | Use case in EventHub | Why this tool |
|---|---|---|
| **PostgreSQL** | System of record: users, events, venues, seats, bookings, payments, `outbox_events`, `idempotency_keys`. ACID booking confirm. | Strong transactions + row-level locking + `jsonb` for outbox payloads. |
| **Flyway** | Versioned schema migrations (`V1__baseline.sql`, `V2__seats.sql`, …); seed data. | Reproducible schema across all envs; no Hibernate auto-DDL in prod. |
| **Spring Data JPA** | Entity mapping, `@Version` optimistic lock, repositories, derived queries. | Declarative persistence; optimistic locking is built-in. |
| **Redis** | Seat-hold TTL keys; waiting-room sorted set + admission tokens; rate-limit buckets; cache (event/venue/trending); optional distributed lock. | O(log N) sorted sets, atomic ops, TTL expiry, Lua scripts. |
| **RabbitMQ** | Async fan-out after `BookingCompleted`: ticket+QR generation, email, invoice, analytics, recommendation-score update, ES indexing. | Topic exchange routing, durable queues, consumer ack/retry/DLQ. |
| **Elasticsearch** | Event search: full-text (name/tags), filters (category/city/date/price), `geo_distance`, faceted aggregations, BM25 relevance. | Purpose-built inverted index; Postgres `LIKE` won't scale. |
| **MinIO / S3** | Store generated ticket PDFs, QR PNGs, event images; serve via pre-signed URLs. | S3-compatible object storage; keeps blobs out of Postgres. |
| **Spring Security + JWT** | Stateless auth, RBAC (CUSTOMER/ORGANIZER/ADMIN), refresh tokens. | Industry-standard, integrates with method security. |
| **Bucket4j (on Redis)** | Token-bucket rate limiting per user/IP on login/booking/payment. | Distributed buckets backed by Redis; clean API. |
| **Spring Retry** | Bounded retry on `OptimisticLockingFailureException` during seat confirm. | Declarative `@Retryable` with backoff. |
| **Spring AI** | Event-copy generation, NL→search-filter assistant, review summarization, embedding-based recs. | `ChatClient` + structured-output `entity()` mapping; provider-portable. |
| **Testcontainers** | Integration tests against real Postgres/Redis/RabbitMQ/Elasticsearch. | Tests hit real infra, not mocks — catches the concurrency bugs. |
| **Docker Compose** | Local full-stack orchestration of all six services. | One `up` for the whole system. |
| **GitHub Actions** | CI: build → test (Testcontainers) → build/push Docker image. | Free CI, Docker-native. |
| **OpenAPI / springdoc** | Auto-generated, browsable API docs at `/swagger-ui`. | Contract surface for reviewers/clients. |

---

## Package Structure

Feature-sliced packages — each owns its entity, repo, service, controller. Cross-cutting infra in `config`/`common`.

```
org.istiaqfuad.eventhub
├── config/          security, redis, rabbit, elasticsearch, openapi, ai beans
├── common/          exceptions, GlobalExceptionHandler, ApiError, pagination, base entity
├── auth/            AuthController, JwtService, RefreshTokenService, filters
├── user/            User, Role, profile
├── event/           Event, Category, Tag, EventService, EventController
│   └── search/      EventDocument (@Document), EventSearchRepository, query builder
├── venue/           Venue, Section, Seat, SeatStatus, layout import
├── booking/         Booking, BookingItem, SeatHoldService, BookingService
│   └── allocation/  SeatRowIndex (segment tree), GroupSeatAllocator
├── payment/         Payment, Refund, PaymentService, IdempotencyService
├── waitingroom/     WaitingRoomService, AdmissionWorker
├── outbox/          OutboxEvent, OutboxRelay
├── messaging/       RabbitConfig, producers, consumers/ (ticket, email, invoice, analytics, rec, indexer)
├── recommendation/  RecommendationService, scoring, TrendingService
├── analytics/       AnalyticsService, dashboard projections
├── ratelimit/       RateLimitFilter, bucket config
├── ai/              ChatClient wrappers, prompt templates, DTO records
└── storage/         MinioClient wrapper, TicketPdfGenerator, QrGenerator
```

---

## Domain Model & Database Design

Core tables and key columns (full DDL lives in Flyway migrations).

| Table | Key columns / notes |
|---|---|
| `users` | id, email (unique), password_hash, enabled, created_at |
| `roles` / `user_roles` | RBAC: CUSTOMER, ORGANIZER, ADMIN |
| `organizers` | user_id FK, org_name, verified |
| `events` | id, organizer_id, title, category_id, city, **geo (lat/lon)**, starts_at, status (DRAFT/ON_SALE/CLOSED), **high_demand bool** |
| `venues` | id, name, layout_type (STADIUM/THEATER/…), address |
| `sections` | id, venue_id, name, seat_type, base_price |
| `seats` | id, section_id, row_label, col_number, **status (FREE/HELD/BOOKED)**, **version (@Version)** |
| `ticket_types` | id, event_id, name, price, quota |
| `bookings` | id, user_id, event_id, status (PENDING/CONFIRMED/CANCELLED), total, created_at |
| `booking_items` | id, booking_id, seat_id, ticket_type_id, price |
| `payments` | id, booking_id, amount, status, provider_ref, idempotency_key |
| `reviews` | id, event_id, user_id, rating, body |
| `notifications` | id, user_id, type, payload, read |
| `refresh_tokens` | id, user_id, token_hash, expires_at |
| `outbox_events` | id (UUID), aggregate_type, aggregate_id, event_type, payload (**jsonb**), status, created_at, processed_at |
| `idempotency_keys` | key (**unique**), request_hash, response (jsonb), status, expires_at |

Key indexes: `seats(section_id, status)`, `outbox_events(processed_at)` partial WHERE NULL, `bookings(user_id)`, `events(status, starts_at)`.

---

## Core Workflow — Ticket Purchase

End-to-end, with the tool used at each step:

```
1. Search event            → Elasticsearch (filters + geo + BM25)
2. [if high_demand] queue  → Redis ZSET waiting room → admission token
3. Select seats            → Redis SETNX hold:{seatId} TTL 10m   (status HELD)
4. Pay                     → Idempotency-Key guard → PaymentService
5. Confirm booking         → Postgres tx: seats HELD→BOOKED (@Version), insert booking,
                             insert outbox_events row   (all atomic)
6. Relay                   → OutboxRelay polls → RabbitMQ "BookingCompleted"
7. Consumers (async)       → ticket PDF+QR → MinIO; email; invoice; analytics; rec-score; ES reindex
8. Download ticket         → pre-signed MinIO URL
```

---

## Feature Designs

### 1. Concurrent Seat Booking — optimistic lock + bounded retry

**Problem.** Two users confirm the same held seat at the same instant → double-book.

**Design.** `Seat.version` (`@Version`). Confirm flips `HELD→BOOKED`; on flush Hibernate checks version. Loser gets `OptimisticLockingFailureException` → `@Retryable` re-reads and re-tries (or surfaces "seat taken"). No DB-level pessimistic lock → high throughput, no lock contention.

```java
@Retryable(retryFor = OptimisticLockingFailureException.class,
           maxAttempts = 3, backoff = @Backoff(delay = 50))
@Transactional
public Booking confirm(Long userId, List<Long> seatIds, String idempotencyKey) {
    idempotency.begin(idempotencyKey, userId, seatIds);   // dedup guard, §5
    var seats = seatRepo.findAllById(seatIds);
    for (var s : seats) {
        if (s.getStatus() != SeatStatus.HELD)
            throw new SeatNotHeldException(s.getId());
        s.setStatus(SeatStatus.BOOKED);                   // version bumped on flush
    }
    var booking = bookingRepo.save(Booking.confirmed(userId, seats));
    outbox.record("Booking", booking.getId(), "BookingCompleted", booking); // same tx, §5
    return booking;
}
```

**Test.** 1000 threads → one seat → assert exactly one CONFIRMED, 999 rejected, zero double-book (Testcontainers + `CountDownLatch`).

### 2. Seat Hold — Redis TTL

**Design.** On select, `SET hold:{seatId} {userId} NX EX 600`. `NX` = only if not already held → atomic guard against concurrent holds. Seat row flips to `HELD`. If payment succeeds → `BOOKED`. If TTL expires → Redis keyspace-notification listener (or lazy check at confirm) flips seat back to `FREE`. Holds never block the DB.

```java
Boolean ok = redis.opsForValue()
    .setIfAbsent("hold:" + seatId, userId.toString(), Duration.ofMinutes(10));
if (!Boolean.TRUE.equals(ok)) throw new SeatAlreadyHeldException(seatId);
```

### 3. Virtual Waiting Room — fair admission queue

**Problem.** Flash-sale: thousands hit booking at once → overload + unfair (bots win) + oversell risk.

**Design.**
- Join: `seq = INCR waitroom:{eventId}:seq` (monotonic, beats clock ties); `ZADD waitroom:{eventId} {seq} {userId}` → strict FIFO.
- Status: position via `ZRANK`; ETA = position / drainRate.
- Admission worker (`@Scheduled`): `ZPOPMIN` a batch sized to booking capacity (token-bucket backpressure); issue token `SET admit:{eventId}:{userId} 1 EX 300`.
- Seat-select endpoint requires a valid admit token → only admitted users reach booking. Unused token expires → slot reclaimed.

```java
public long join(long eventId, long userId) {
    long seq = redis.opsForValue().increment("waitroom:" + eventId + ":seq");
    redis.opsForZSet().add("waitroom:" + eventId, String.valueOf(userId), seq);
    return redis.opsForZSet().rank("waitroom:" + eventId, String.valueOf(userId));
}

@Scheduled(fixedDelay = 1000)               // drain rate = batch per second
public void admit() {
    for (long eventId : highDemandEvents()) {
        var batch = redis.opsForZSet().popMin("waitroom:" + eventId, DRAIN_PER_SEC);
        batch.forEach(t -> redis.opsForValue()
            .set("admit:" + eventId + ":" + t.getValue(), "1", Duration.ofMinutes(5)));
    }
}
```

All ops O(log N). Stale users evicted via heartbeat TTL; reconnect idempotent (membership keyed by userId).

### 4. Contiguous Group-Seat Allocation — max-contiguous-segment segment tree

**Problem.** Group of `k` wants adjacent seats in one row. Naive scan slow + race-prone.

**Design.** Per row, a segment tree over seat columns; each node stores `{prefixFree, suffixFree, maxFree, len}`. Merge children → max contiguous free run. Query "first block of ≥ k" and point-update on book/release, both **O(log n)**. On release, freed neighbours coalesce naturally (interval merge / union-find view). Chosen block reserved atomically under the §1/§2 hold path; version conflict → retry, recompute block.

```java
class SeatRowIndex {                 // segment tree, one per (section,row)
    int findBlockStart(int k);       // -1 if no run ≥ k; O(log n)
    void setTaken(int col);          // point update; O(log n)
    void setFree(int col);
}

// GroupSeatAllocator
int start = rowIndex.findBlockStart(count);
if (start < 0) throw new NoContiguousBlockException();
var seats = seatRepo.findRowRange(sectionId, rowLabel, start, start + count);
holdService.holdAll(seats, userId);   // §2 atomic hold
```

**Test.** Deterministic block correctness + two groups racing one row → no overlap.

### 5. Reliable Delivery — transactional outbox + idempotency

**Problem A (dual-write).** Confirm in Postgres then publish to RabbitMQ — crash between = lost event (no ticket/email). Can't 2PC Postgres+RabbitMQ.
**Problem B (double-charge).** Client retry of `POST /api/payments` → duplicate charge.

**Outbox.** Insert `outbox_events` row *inside the booking transaction* → atomic single-DB write. Relay polls unprocessed rows, publishes, marks processed → at-least-once. Consumers idempotent (dedup by event id).

```java
@Scheduled(fixedDelay = 500)
@Transactional
public void relay() {
    for (var e : outboxRepo.findTop100ByProcessedAtIsNullOrderByCreatedAt()) {
        rabbit.convertAndSend("eventhub.exchange", e.getEventType(), e.getPayload());
        e.setProcessedAt(Instant.now());
    }
}
```

**Idempotency.** `Idempotency-Key` header → `idempotency_keys.key` has a unique constraint. First request `INSERT ... ON CONFLICT DO NOTHING`; winner processes and stores the response; a retry with the same key returns the stored response without re-executing.

### 6. Event-Driven Consumers

Topic exchange `eventhub.exchange`. `BookingCompleted` fans out to durable queues, each with its own consumer, manual ack, and a dead-letter queue for poison messages:

- `ticket.generate` → render PDF + QR → MinIO → store URL
- `email.send` → confirmation email
- `invoice.generate` → invoice PDF → MinIO
- `analytics.update` → increment sales/occupancy projections
- `recommendation.update` → bump event score
- `search.index` → upsert `EventDocument` in Elasticsearch

### 7. Event Search — Elasticsearch

`EventDocument` (`@Document(indexName="events")`) with `@GeoPointField location`. A `bool` query: `must` full-text (name/tags, BM25), `filter` term/range (category, city, date, price), optional `geo_distance`. Aggregations drive category/city facets. Index kept in sync by the `search.index` consumer (derived from Postgres via outbox → never the write path).

### 8. Recommendation + Trending + Caching

**Weighted score** (Postgres-driven, deterministic, cold-start safe):
```
score = 0.35·categoryMatch + 0.25·popularity + 0.20·userInterest + 0.20·recentActivity
```
New users with no history → fall back to trending. **Trending (Top-K):** rolling booking counts in a bounded min-heap of size K. **Caching:** `@Cacheable` (Redis) on event detail, venue, categories, trending, and search results; evict on update. (AI-embedding variant in AI Features.)

### 9. Rate Limiting

Bucket4j token buckets backed by Redis, applied in a servlet filter keyed by user/IP. Protects `login`, `booking`, `payment`. A hand-rolled Redis Lua **sliding-window** counter is included as the algorithm showcase (atomic increment + window trim in one round-trip).

### 10. Analytics

Organizer dashboard projections updated by the `analytics.update` consumer: revenue, tickets sold, occupancy rate, popular events, daily sales, booking trends. Read from precomputed projection tables, not live aggregates over `bookings`.

---

## AI Features (Spring AI — Phase 9, assist layer)

AI assists; it never gates core flows. All via `ChatClient` with structured-output `entity()` mapping to Java records.

**Event description.** Organizer enters title/category/keywords → generate copy:
```java
record EventCopy(String description, String marketingSummary, List<String> seoKeywords) {}

EventCopy copy = chatClient.prompt()
    .user(u -> u.text("Write event copy for title={title}, category={cat}, keywords={kw}")
                .param("title", title).param("cat", category).param("kw", keywords))
    .call()
    .entity(EventCopy.class);
```

**Search assistant.** NL → structured filter, then feed to the §7 Elasticsearch query:
```java
record SearchFilters(String category, BigDecimal maxPrice, String city, String dateRange) {}
// "Show me concerts under $50 this weekend" → SearchFilters → ES bool query
SearchFilters f = chatClient.prompt().user(nlQuery).call().entity(SearchFilters.class);
```

**Review summarization.** System prompt + `entity(ReviewSummary.class)` over batched reviews → highlights.

**AI recommendation.** `EmbeddingModel` → event embeddings in a vector store (pgvector) → cosine similarity vs the user's interest vector. Falls back to the §8 weighted score on cold start. Resilience4j circuit breaker around all model calls; AI failure degrades gracefully to non-AI behaviour.

---

## Algorithms & Data Structures

**Concurrency & correctness** — optimistic locking (`@Version`) + bounded retry; transactional outbox; idempotency-key dedup (unique-constraint race resolution).
**Allocation** — max-contiguous-segment segment tree + interval union-find; bitmap + sliding-window best-fit.
**Traffic control** — fair FIFO admission queue (Redis ZSET, O(log N)); token-bucket / Lua sliding-window rate limiting.
**Ranking & search** — weighted recommendation scoring (+ cold-start fallback); Top-K trending (min-heap); Elasticsearch BM25 over filtered/geo/paginated queries.
**Caching** — Redis TTL seat-hold expiration; `@Cacheable` read-through.

---

## REST API

```
Auth      POST /api/auth/register | /login | /refresh
Events    GET /api/events | GET /api/events/{id} | POST | PUT /{id} | DELETE /{id}
Search    GET /api/events/search?q=&category=&city=&maxPrice=&near=lat,lon&page=
Booking   POST /api/bookings            (Idempotency-Key header)
          POST /api/bookings/group      {eventId, sectionId, count, preference} → contiguous seats
          GET  /api/bookings | DELETE /api/bookings/{id}
Holds     POST /api/holds {seatIds}     | DELETE /api/holds/{seatId}
Queue     POST /api/events/{id}/queue/join   → position + admission ticket
          GET  /api/events/{id}/queue/status → position, ETA, admitted?
Payments  POST /api/payments (Idempotency-Key) | POST /api/refunds
Recs      GET /api/recommendations
AI        POST /api/ai/event-copy | GET /api/ai/search?q=<natural language>
```

OpenAPI/Swagger UI at `/swagger-ui`.

---

## Security

JWT (access + refresh), BCrypt hashing, method-level RBAC (`@PreAuthorize`), CORS config, Bean Validation on all inputs, `@RestControllerAdvice` global exception handler returning RFC-7807 `ApiError`. Rate limiting on auth/booking/payment (§9).

---

## Testing Strategy

- **Unit** — services with Mockito (scoring, allocation segment tree, idempotency logic).
- **Integration** — Testcontainers spin real Postgres/Redis/RabbitMQ/Elasticsearch.
- **Concurrency** — the 1000-thread / one-seat double-book test; two-group / one-row allocation race.
- **API** — `@SpringBootTest` + MockMvc over controllers.
- **Outbox** — kill relay mid-publish, assert no event loss on restart.

---

## Deployment

`docker-compose.yml` services: app (Spring Boot), postgres, redis, rabbitmq, elasticsearch, minio. Each with healthchecks; app waits on dependencies.

**CI/CD (GitHub Actions):** build → run tests (Testcontainers) → build & push Docker image. Flyway migrates on startup.

---

## Implementation Roadmap

Build core-first; AI and analytics last. Each phase ships with its tests.

| Phase | Deliverable | Key tools |
|---|---|---|
| 0 | Scaffolding, docker-compose, Flyway baseline, OpenAPI | Compose, Flyway |
| 1 | Auth + RBAC (JWT, refresh, BCrypt) | Spring Security |
| 2 | Event + Venue + Section + Seat domain + CRUD | JPA, Flyway |
| 3 | **Seat hold + concurrent booking + payment + idempotency** (concurrency tests) | Redis, `@Version`, Spring Retry |
| 4 | Outbox + RabbitMQ + consumers (ticket/QR→MinIO, email, invoice, analytics) | RabbitMQ, MinIO |
| 5 | Elasticsearch indexing + search (filters, geo, BM25) | Elasticsearch |
| 6 | Recommendation + trending + Redis caching | Redis |
| 7 | Virtual waiting room + rate limiting | Redis ZSET, Bucket4j |
| 8 | Contiguous group-seat allocation (segment tree) | — |
| 9 | AI features (event copy, NL search, review summary, embeddings) | Spring AI |
| 10 | Analytics dashboard + CI/CD hardening | GitHub Actions |

Phases 3–5 are the resume core. AI (9) and analytics (10) are bolt-on; cutting them leaves a fully working platform.

---

## Future Enhancements

Microservice split · WebSocket live seat availability · dynamic pricing · QR check-in service · fraud detection · waitlist · coupon engine · loyalty program · i18n · multi-tenant organizations.

---

## Resume Highlights

- Built a production-grade event-ticketing backend (Spring Boot 3, Java 21, PostgreSQL) with a Postgres-first, event-driven architecture.
- Implemented safe concurrent seat booking with JPA optimistic locking and bounded retry; verified with a 1000-thread test proving zero double-booking.
- Engineered a fair virtual waiting room (Redis sorted-set FIFO + token-bucket admission) to absorb flash-sale spikes without overloading the booking service.
- Built contiguous group-seat allocation on a max-contiguous-segment segment tree for O(log n) best-block queries under concurrency.
- Guaranteed reliable ticketing via the transactional outbox pattern and idempotency keys — eliminating dual-write event loss and duplicate charges.
- Designed RabbitMQ event-driven fan-out for async ticket/QR generation, email, invoicing, and analytics.
- Implemented Elasticsearch search (full-text, geo, faceted) and Redis caching with TTL-based seat holds.
- Added Spring AI assist features: event-copy generation, natural-language search, review summarization, and embedding-based recommendations.
- Containerized with Docker Compose and shipped CI/CD (GitHub Actions) with Testcontainers integration tests.
