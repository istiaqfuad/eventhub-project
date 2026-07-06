# EventHub API — Testing Guide

How to exercise every request in the **EventHub API** Postman collection against a
locally running server. Covers prerequisites, the run order, each endpoint's expected
result, and the two flows that need extra setup (privileged roles, Stripe payment).

---

## 1. Prerequisites

1. **App running** on `http://localhost:8080` (IntelliJ or `./mvnw spring-boot:run`).
2. **Environment variables** (the app fails fast without the required ones):

   | Var | Required | Notes |
   |-----|----------|-------|
   | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | yes | Postgres |
   | `JWT_SECRET` | yes | Base64, ≥ 256 bits |
   | `STRIPE_SECRET_KEY` | yes* | `sk_test_…` / `rk_test_…` — needed for the app to boot |
   | `STRIPE_WEBHOOK_SECRET` | yes* | `whsec_…` from `stripe listen` |
   | `COOKIE_SECURE` | no | set `false` for plain-HTTP local dev |

   \* Since the Stripe config has no defaults, the app will not start unless these are set.
3. **Seed fixture** present (a bookable event with a venue, free seats, and a GA ticket
   type). See [§6](#6-seed-fixture). The collection variables `eventId`, `seatId`,
   `ticketTypeId` point at it.
4. **Import** the `EventHub API` collection and select the `EventHub Local` environment
   (or set the `baseUrl` collection variable to `http://localhost:8080`).

Run either from the **Postman app** (Collection Runner) or the CLI:

```bash
newman run "EventHub API.postman_collection.json" -e "EventHub Local.postman_environment.json"
```

---

## 2. How the variables chain

Requests capture ids into collection variables that later requests reuse. Run folders
**top to bottom**:

| Variable | Set by | Used by |
|----------|--------|---------|
| `accessToken` | Auth → Login | every bearer request |
| `userId` | Auth → Login | Get current user, Get booking (ownership) |
| `bookingId` | Bookings → seated create | Get booking, Initiate payment |
| `paymentId` | Payments → initiate | Get payment |
| `venueId` | Venues → create (privileged) | Get venue |
| `eventId`, `seatId`, `ticketTypeId` | preset to the seed fixture | Bookings |

**Run order:** `Auth` → `Users` → `Venues` → `Events` → `Bookings` → `Payments` →
`Reviews` → `Versioning`.

---

## 3. Endpoints by folder

All error bodies are RFC 9457 `application/problem+json` with a stable `code`.
Bearer requests send `Authorization: Bearer {{accessToken}}`.

### Auth
| # | Request | Method · Path | Auth | Expect |
|---|---------|---------------|------|--------|
| 1 | Register (valid) | `POST /api/auth/register` | public | 201, user JSON (role CUSTOMER) |
| 2 | Register (compromised password) | `POST /api/auth/register` | public | 400 `COMPROMISED_PASSWORD` (HaveIBeenPwned) |
| 3 | Login | `POST /api/auth/login` | public | 200 + `accessToken`; `Set-Cookie` refresh + `XSRF-TOKEN` |
| 4 | Get current user | `GET /api/users/{{userId}}` | bearer | 200, id == `userId` |
| 5 | Protected without token | `GET /api/users/{{userId}}` | none | 401 |
| 6 | Refresh without CSRF | `POST /api/auth/refresh` | cookie | 403 `ACCESS_DENIED` (double-submit CSRF) |
| 7 | Refresh (with CSRF) | `POST /api/auth/refresh` | cookie + `X-XSRF-TOKEN` | 200, rotates the refresh token |
| 8 | Logout | `POST /api/auth/logout` | cookie + CSRF | 204, clears the cookie |

### Users
| Request | Method · Path | Auth | Expect |
|---------|---------------|------|--------|
| Register user (invalid) | `POST /api/auth/register` | public | 400 `VALIDATION_ERROR` (+ `errors[]`) |
| Get user by id | `GET /api/users/{{userId}}` | bearer (self) | 200 |
| Get another user's profile | `GET /api/users/999999` | bearer | 403 `ACCESS_DENIED` (self-or-ADMIN; no existence leak) |

### Venues
| Request | Method · Path | Auth | Expect |
|---------|---------------|------|--------|
| Create venue (valid, ORGANIZER/ADMIN) | `POST /api/venues` | bearer | 201 with a privileged token; **403 as a CUSTOMER** — see [§4](#4-getting-an-organizeradmin-token) |
| Create venue (invalid) | `POST /api/venues` | bearer | 400 `VALIDATION_ERROR` |
| Get venue by id | `GET /api/venues/{{venueId}}` | public | 200 |
| List venues | `GET /api/venues` | public | 200 |
| Create venue as CUSTOMER | `POST /api/venues` | bearer (CUSTOMER) | 403 `ACCESS_DENIED` (role gate) |

### Events
| Request | Method · Path | Auth | Expect |
|---------|---------------|------|--------|
| Create event (valid, ORGANIZER/ADMIN) | `POST /api/events` | bearer | 201 with a privileged token; **403 as a CUSTOMER**. Organizer is derived from the caller (body `organizerId` ignored unless ADMIN) |
| Create event (invalid) | `POST /api/events` | bearer | 400 `VALIDATION_ERROR` |
| Get event by id | `GET /api/events/{id}` | public | 200 |
| Create event as CUSTOMER | `POST /api/events` | bearer (CUSTOMER) | 403 `ACCESS_DENIED` (role gate) |

### Bookings
| Request | Method · Path | Auth | Expect |
|---------|---------------|------|--------|
| Create booking — seated (valid) | `POST /api/bookings` | bearer | 201 PENDING, `total` from the seat's section, seat → HELD; captures `bookingId` |
| Reserve held seat | `POST /api/bookings` (same seat) | bearer | 409 `RESERVATION_CONFLICT` (seat now HELD) |
| Create booking — GA (valid) | `POST /api/bookings` | bearer | 201 (or 409 `RESERVATION_CONFLICT` once the ticket type's quota is exhausted) |
| Create booking (invalid: empty items) | `POST /api/bookings` | bearer | 400 |
| Create booking (invalid: neither seat nor ticket) | `POST /api/bookings` | bearer | 400 |
| Get booking by id | `GET /api/bookings/{{bookingId}}` | bearer (owner) | 200 with line items; 403 for a non-owner |

Holds are released automatically ~15 min later (or at the Stripe session expiry once a
payment is initiated) by the scheduled sweeper.

### Reviews
| Request | Method · Path | Auth | Expect |
|---------|---------------|------|--------|
| Create review (valid) | `POST /api/reviews` | bearer | 201 (one review per user per event) |
| Create review (invalid: rating out of range) | `POST /api/reviews` | bearer | 400 `VALIDATION_ERROR` |
| Get review by id | `GET /api/reviews/{id}` | public | 200 (reviews are public) |

### Payments
| Request | Method · Path | Auth | Expect |
|---------|---------------|------|--------|
| Initiate payment — Stripe Checkout | `POST /api/payments` | bearer (owner) | 201 PENDING, `providerRef` = `cs_…`, `checkoutUrl`; captures `paymentId`. Needs a PENDING `bookingId`. See [§5](#5-stripe-payment-flow) |
| Create payment (invalid) | `POST /api/payments` | bearer | 400 `VALIDATION_ERROR` (missing `bookingId`) |
| Get payment by id | `GET /api/payments/{{paymentId}}` | bearer (owner) | 200; `checkoutUrl` null on read |
| _Webhook_ | `POST /api/payments/webhook` | signature | **Not Postman-testable** — Stripe calls it; verified by signature. Use the Stripe CLI ([§5](#5-stripe-payment-flow)) |

### Versioning
| Request | Method · Path | Expect |
|---------|---------------|--------|
| v1 media type | `GET /api/version` `Accept: …;version=1` | 200 `{"version":"1"}` |
| v2 media type | `GET /api/version` `Accept: …;version=2` | 200 `{"version":"2"}` |
| no version | `GET /api/version` | 200 `{"version":"1"}` (lenient default) |
| v3 unsupported | `GET /api/version` `Accept: …;version=3` | 400 |

---

## 4. Getting an ORGANIZER/ADMIN token

Registration only grants `CUSTOMER`, so `POST /events` and `POST /venues` return **403**
with a self-registered token — that is the role gate working. To exercise the 201 happy
paths, grant a role out of band, then **log in again** (the role travels in a fresh JWT):

```sql
-- grant ORGANIZER to a user, and give them an organizer profile (events derive the organizer from it)
INSERT INTO user_roles (user_id, role_id)
  SELECT :userId, id FROM roles WHERE name = 'ORGANIZER'
  ON CONFLICT DO NOTHING;
INSERT INTO organizers (user_id, org_name, verified)
  VALUES (:userId, 'Test Org', true);
```

Re-run **Login** to refresh `accessToken`, then `Create event/venue (valid)` returns 201.

---

## 5. Stripe payment flow

Confirmation is webhook-driven — Postman only initiates; the rest happens through Stripe.

1. Start the CLI forwarder (**same account as `STRIPE_SECRET_KEY`**; each run prints a
   fresh signing secret):
   ```bash
   stripe listen --forward-to localhost:8080/api/payments/webhook
   ```
   Put the printed `whsec_…` into `STRIPE_WEBHOOK_SECRET` and restart the app if it changed.
2. In Postman: **Bookings → seated create** (captures `bookingId`), then
   **Payments → Initiate payment** → copy `checkoutUrl`.
3. Open `checkoutUrl`, pay with test card `4242 4242 4242 4242` (any future expiry, any
   CVC/ZIP). The `success_url` (`localhost:5173`) is a placeholder — a "can't connect"
   page there is cosmetic; the payment still succeeded.
4. `stripe listen` forwards `checkout.session.completed → [200]`. The app sets the payment
   SUCCEEDED, the booking CONFIRMED, and its seats BOOKED.
5. Verify: **Payments → Get payment** shows `SUCCEEDED`; **Bookings → Get booking** shows
   `CONFIRMED`.

Troubleshooting: `stripe listen` shows nothing → CLI on a different account than the app
key (bind it with `stripe listen --api-key "$STRIPE_SECRET_KEY" …`); a `[400]` → the
`whsec_` doesn't match `STRIPE_WEBHOOK_SECRET`; redeliver a past event with
`stripe events resend <evt_id>`.

---

## 6. Seed fixture

The Bookings/Payments requests need a bookable event. Seed one (adjust ids to taste):

```sql
WITH v AS (
  INSERT INTO venues (name, layout_type, address, city)
  VALUES ('Test Arena', 'STADIUM', '1 St', 'Metro') RETURNING id
), s AS (
  INSERT INTO sections (venue_id, name, seat_type, base_price)
  SELECT id, 'A', 'REGULAR', 50.00 FROM v RETURNING id
), seat AS (
  INSERT INTO seats (section_id, row_label, col_number, status)
  SELECT id, 'A', 1, 'FREE' FROM s RETURNING id
), u AS (
  INSERT INTO users (email, password_hash, enabled, public_id)
  VALUES ('org-'||floor(random()*1e9)||'@ex.com', '{noop}x', true, gen_random_uuid()) RETURNING id
), o AS (
  INSERT INTO organizers (user_id, org_name, verified) SELECT id, 'Org', true FROM u RETURNING id
), e AS (
  INSERT INTO events (organizer_id, title, venue_id, status, high_demand, starts_at, ends_at, public_id)
  SELECT o.id, 'Test Event', v.id, 'ON_SALE', false, now()+interval '10 day', now()+interval '10 day 3 hour', gen_random_uuid()
  FROM o, v RETURNING id
)
INSERT INTO ticket_types (event_id, name, price, quota, sold)
  SELECT id, 'GA', 25.00, 5, 0 FROM e
  RETURNING (SELECT id FROM e) AS event_id, (SELECT id FROM seat) AS seat_id, id AS ticket_type_id;
```

Set the returned ids into the collection variables `eventId`, `seatId`, `ticketTypeId`.

**Reset between runs** (release test bookings, free the inventory) so the seated/GA
happy paths can run again:

```sql
DELETE FROM payments WHERE booking_id IN (SELECT id FROM bookings WHERE event_id = :eventId);
DELETE FROM bookings WHERE event_id = :eventId;                 -- booking_items cascade
UPDATE seats SET status = 'FREE'
  WHERE section_id IN (SELECT id FROM sections WHERE venue_id = :venueId);
UPDATE ticket_types SET sold = 0 WHERE event_id = :eventId;
```
