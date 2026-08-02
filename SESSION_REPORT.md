# EventHub — Session Report: Server Log Analysis & Fixes

**Date:** 2026-08-03
**Branch:** `fix/security-and-neon-cpu` (6 commits, based on `main`)
**Scope:** Analysis of `server_logs/` + fixes for Neon CPU drain, backend crash-loop, and exposed infrastructure.

---

## 1. What was analyzed

Three container logs from `server_logs/` (collected 2026-08-02):

| Log | Time span | What it showed |
|---|---|---|
| `eventhub-backend-*.log` | 2026-08-02 22:28–22:30 | 4 startup attempts, **all failed** — app never started |
| `eventhub-rabbitmq-*.log` | 2026-07-20 → 08-02 | Broker up, but hammered by public internet scanners |
| `eventhub-redis-*.log` | 2026-07-28 → 08-02 | **Active exploitation** — replication hijack attempts |

---

## 2. Problems found

### 🔴 Critical — Redis compromised / actively attacked
- Redis exposed on `0.0.0.0:6379` with **no password** (`docker-compose.yml` mapped the port publicly).
- Log evidence of takeover attempts:
  - 192 replica-sync attempts to 13 foreign IP:port combos (Alibaba/Tencent Cloud ranges: `120.77.237.174`, `47.76.79.220`, `60.205.248.70`, `43.134.91.158`, `103.74.95.131`, `213.136.79.115:21000-21011`).
  - Remote `slaveof` command executed from `47.103.23.97` — attacker flipped Redis to slave mode and back.
  - `Wrong signature trying to load DB from file` — corrupted RDB, signature of the **Redis replication attack** (plant SSH keys / cron / webshells, or exfiltrate data via `REPLICAOF`).

### 🔴 Critical — Backend crash-looping, app never starts
- `java.net.UnknownHostException: eventhub-elasticsearch` during `EventSearchRepository` bean creation.
- `EventSearchSyncConsumer` hard-depends on Elasticsearch at startup → whole Spring context aborts.
- `restart: unless-stopped` → restart forever (~every 30–60s).
- Result: **backend fully down** for the deployment.

### 🟠 Neon CPU rising with "no usage" — root cause
Two compounding causes:

1. **Crash-loop connection churn (happening now).** Every restart cycle: Hikari connection open → Flyway validation → shutdown → repeat. ~2,000+ fresh connections/day to Neon with zero traffic. Connection churn is expensive on Neon serverless.

2. **Idle DB polling (keeps CPU high once app runs).** Three schedulers hit Postgres 24/7:

   | Scheduler | Frequency | Query | Idle cost/day |
   |---|---|---|---|
   | `AdmissionWorker.admit()` | every 1s | `events.findAll()` — **full table load**, filtered in Java | 86,400 full scans |
   | `OutboxRelay.relay()` | every 500ms | `findTop100ByStatus...` on `outbox_events` | 172,800 queries |
   | `BookingExpiryService.scheduledSweep()` | every 60s | `findExpiredPending` | 1,440 queries |

   ≈ **~260,000 polling queries/day with no users.**

3. **No Hikari tuning.** Defaults (max pool 10, keep idle connections) are wrong for serverless Neon.

### 🟠 Exposed infrastructure (RabbitMQ, Elasticsearch)
- **RabbitMQ** `5672` + `15672` (management UI) public, default `guest/guest`. Log: 212 AMQP connections from public IPs — HTTP GET probes, TLS ClientHellos, `MGLNDD_2` scanner banner, AMQP-1.0 probes.
- **Elasticsearch** `9200` public with `xpack.security.enabled=false` — anyone can read/write/delete the search index.

### 🟡 Hygiene
- README referenced a `postgres` service that doesn't exist (DB is Neon); missing env-var documentation.
- `server_logs/` not gitignored.

---

## 3. Fixes applied (6 commits)

| Commit | File(s) | Change |
|---|---|---|
| `0deb584` fix(infra) | `docker-compose.yml`, `.env.example`, `application.properties` | Redis/RabbitMQ ports bound to `127.0.0.1` only; ES port mapping removed (internal only). Redis requires `REDIS_PASSWORD`, RabbitMQ requires `RABBITMQ_USER`/`RABBITMQ_PASS` — compose **fails fast** if unset. Backend passes/reads the Redis password. |
| `7e862a2` fix(search) | `EventSearchSyncConsumer.java`, `EventSearchRepository.java` | Repo marked `@Lazy` (no ES ping at startup); consumer ES writes wrapped in try/catch (log + ack instead of poison-message requeue). **App now starts with ES down.** |
| `af82d44` fix(waitingroom) | `AdmissionWorker.java`, `EventRepository.java` | `events.findAll()` → indexed `findByHighDemandTrue()`. Poll interval configurable via `app.waiting-room.poll-delay`. |
| `3b8081f` fix(outbox) | `OutboxRelay.java`, `OutboxRelayScheduler.java` (new) | `OutboxRelay` returns processed count; new scheduler drives adaptive backoff — 500ms while draining, **5s while idle**. |
| `d717a4c` chore(datasource) | `application.properties` | Hikari: `maximum-pool-size=5` (configurable), `minimum-idle=0`, `max-lifetime=1200000`, `connection-timeout=30000`. |
| `3e9b2ea` docs | `README.md`, `.gitignore` | Quickstart matches Neon reality + documents required env vars; `server_logs/` ignored. |

**Verified:** `./mvnw compile` and `test-compile` exit 0; `docker compose config` valid; compose correctly refuses to start without the new secrets.

---

## 4. What to do before deployment

### Step 1 — Add required secrets
Add to the deployment environment (dokploy / `.env`):

```
REDIS_PASSWORD=your_strong_redis_password
RABBITMQ_USER=eventhub
RABBITMQ_PASS=your_strong_rabbitmq_password
```

Compose **will not start** without them (intentional fail-fast). Existing `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, Stripe vars stay as-is.

### Step 2 — Incident response on Redis (critical)
The old Redis was actively exploited. Before or right after redeploy:

1. After the container comes up with the new password:
   ```bash
   redis-cli -h 127.0.0.1 -p 6379 -a "$REDIS_PASSWORD" REPLICAOF NO ONE
   redis-cli -h 127.0.0.1 -p 6379 -a "$REDIS_PASSWORD" CONFIG REWRITE
   ```
   (Stale `replicaof` config can persist in the `redis-data` volume.)
2. Check the host for planted artifacts — the replication attack can write SSH keys, cron jobs, or webshells:
   - `~/.ssh/authorized_keys` on the dokploy host
   - `/var/spool/cron/`, `/etc/cron.*`
   - recently modified files in `/tmp`, `/var/www`, web roots
3. Treat anything that was in Redis as leaked — especially **JWT refresh tokens** (stored in Redis). Rotate `JWT_SECRET` to force re-login of all users, or rely on refresh-token rotation in code. Rate-limit state is ephemeral and safe.

### Step 3 — Restart with new credentials
- RabbitMQ now requires `RABBITMQ_USER`/`RABBITMQ_PASS` — the old `guest/guest` virtual host data stays, but any external tooling that connected with `guest/guest` (local scripts, monitoring) must be updated.
- Verify the backend can reach all three: `docker compose logs backend` should show a clean startup (no `UnknownHostException`).

### Step 4 — Ensure Elasticsearch is actually running
- The code now tolerates ES being down (no crash), but search stays stale until ES is up **on `dokploy-network`** with the service name `eventhub-elasticsearch`.
- If the ES container isn't running on the node, deploy/start it — otherwise search features return empty results.

### Step 5 — Post-deploy verification checklist
- [ ] `docker compose ps` — all services `Up` (no restart loop on backend)
- [ ] Backend log contains `Started EventhubApplication`
- [ ] `redis-cli ping` requires the password (unauth = `NOAUTH`)
- [ ] From an external host: `nc -zv <server-ip> 6379 5672 15672 9200` → all **refused/timeout** (except 8080/3001 behind the proxy)
- [ ] RabbitMQ management UI (`127.0.0.1:15672`) rejects `guest/guest`, accepts `RABBITMQ_USER`
- [ ] Neon CPU graph: flatline when idle (was: sawtooth from crash-loop + polling)

---

## 5. Recommended follow-ups (not done in this session)

1. **Healthchecks + startup ordering** — add `healthcheck` to redis/rabbitmq/es and `depends_on: condition: service_healthy` so backend starts only after infra is ready (currently handled in code, ordering is best-effort).
2. **Enable Elasticsearch security** (`xpack.security`) since it's no longer publicly reachable but still unauthenticated on the internal network.
3. **Reconcile compose files** — root `docker-compose.yml` (deployment) vs `eventhub/docker-compose.yml` (local dev) drift: postgres/minio only in the inner one, ES `8.10.2` vs `9.4.2`.
4. **Gate the waiting-room poller** on a Redis flag/queue existence so even the 1/s indexed query stops when no waiting room is active.
5. **ES 9.4.2 heap** — 512m is tight; raise to 1g if the node allows, to reduce OOM risk.
