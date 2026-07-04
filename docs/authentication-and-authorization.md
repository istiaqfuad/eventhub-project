# EventHub — Authentication & Authorization

A complete reference for how EventHub authenticates users and authorizes requests.
Stack: Spring Boot 4.1, Spring Security 7, Java 25, jjwt 0.13, PostgreSQL.

---

## 1. Overview

EventHub is a **stateless** REST API. There are no server sessions; every protected
request proves identity with a signed **access token** (JWT) in the `Authorization`
header. A separate long-lived **refresh token** (opaque, stored hashed, delivered as an
httpOnly cookie) lets clients mint fresh access tokens without re-entering credentials.

Two independent authentication paths share one `AuthenticationManager`:

| Path | When | Mechanism | DB hit? |
|------|------|-----------|---------|
| **Password login** | `POST /api/auth/login` | `DaoAuthenticationProvider` (bcrypt) | yes — load user |
| **Bearer request** | any request with `Authorization: Bearer …` | custom `JwtAuthenticationProvider` | no — trusts signed claims |

Authorization is **coarse** today: a single public/private boundary plus role-based
method security (`@PreAuthorize`) available for per-endpoint tightening.

---

## 2. Design decisions

| # | Decision | Choice | Rationale |
|---|----------|--------|-----------|
| 1 | Access token | HS256 JWT, ~15 min | Stateless verification, no DB per request |
| 2 | Refresh token transport | httpOnly + Secure + SameSite=Strict cookie | Not readable by JS (XSS-resistant); browser sends it automatically |
| 3 | Refresh storage | opaque random, **SHA-256 hashed** in `refresh_tokens` | A DB leak does not expose usable tokens |
| 4 | Rotation | new refresh token on every use; **reuse detection** | Limits the blast radius of a stolen token |
| 5 | Compromised-password check | HaveIBeenPwned, **registration only** | Blocks breached passwords without adding an external call to the login hot path |
| 6 | CSRF | enabled but **scoped** to the cookie endpoints | Bearer endpoints are CSRF-immune; only cookie-authenticated `refresh`/`logout` need it |
| 7 | Auditor | authenticated user id (`Long`) | FK-joinable `created_by`/`updated_by` for auditing |

---

## 3. Token model

### 3.1 Access token (JWT)

- **Algorithm:** HS256 (jjwt). Signing key derived from `app.jwt.secret` (Base64,
  must decode to ≥ 256 bits).
- **Claims:** `iss=eventhub`, `sub=<userId>`, `email`, `roles` (list of role names),
  `iat`, `exp`.
- **Lifetime:** `app.jwt.access-ttl` (default `PT15M`).
- **Transport:** `Authorization: Bearer <jwt>`.
- **Validation:** fully stateless — signature + issuer + expiry checked, authorities
  built from the `roles` claim. No database read.
- **Code:** `security/jwt/JwtService.java` (`generateAccessToken`, `parse`).

### 3.2 Refresh token (opaque)

- **Value:** 256 bits from `SecureRandom`, Base64URL (no padding). The **raw** value is
  handed to the client cookie; only its **SHA-256 hex hash** is persisted in
  `refresh_tokens.token_hash` (UNIQUE).
- **Lifetime:** `app.jwt.refresh-ttl` (default `P30D`).
- **Cookie:** `refresh_token`; `HttpOnly; Secure; SameSite=Strict; Path=/api/auth`
  (`Secure` toggled by `app.security.cookie.secure`).
- **Rotation:** every successful `/api/auth/refresh` revokes the presented token and
  issues a new one.
- **Reuse detection:** presenting an already-revoked token ⇒ assume theft ⇒ revoke the
  user's entire token family and reject with 401.
- **Code:** `auth/service/RefreshTokenService.java` (`issue`, `rotate`, `revoke`).

---

## 4. Components

```
security/
  SecurityConfig            SecurityFilterChain + AuthenticationManager, PasswordEncoder,
                            CompromisedPasswordChecker, CorsConfigurationSource beans
  SecurityPaths             single source of truth for the public/private boundary
  JwtProperties             @ConfigurationProperties("app.jwt")   — secret, issuer, TTLs
  CorsProperties            @ConfigurationProperties("app.cors")  — allowed origins
  CookieProperties          @ConfigurationProperties("app.security.cookie") — secure, name
  jwt/
    JwtService              issue/verify HS256 tokens
    JwtAuthenticationToken  Authentication carrying the raw token / authenticated principal
    JwtAuthenticationProvider  validates the JWT, builds authorities from claims (no DB)
    JwtAuthenticationFilter    OncePerRequestFilter → AuthenticationManager
  userdetails/
    AppUserPrincipal        UserDetails wrapper; exposes userId + ROLE_* authorities
    AppUserDetailsService   loadUserByUsername(email) with roles eagerly fetched
  web/
    ProblemAuthenticationEntryPoint  401 → RFC 9457 problem+json
    ProblemAccessDeniedHandler       403 → RFC 9457 problem+json
    CsrfCookieFilter                 materializes the XSRF-TOKEN cookie
auth/
  controller/AuthController        /register /login /refresh /logout
  service/AuthService              orchestration (register + token lifecycle)
  service/RefreshTokenService      issue / rotate / revoke / reuse-detect
  service/InvalidRefreshTokenException
  repository/RefreshTokenRepository  findByTokenHash, revokeAllForUser
  dto/LoginRequest, TokenResponse
user/
  repository/RoleRepository        findByName
  repository/UserRepository        findByEmail (@EntityGraph roles), existsByEmail
common/config/AuditingConfig       AuditorAware ← SecurityContext principal
common/exception/GlobalExceptionHandler  auth errors → problem+json
```

---

## 5. Authentication flows

### 5.1 Register — `POST /api/auth/register`

```
client → { email, password }
  1. Bean validation (email format, password 8–72 chars)
  2. existsByEmail(email)?  → 409 DUPLICATE_RESOURCE
  3. CompromisedPasswordChecker.check(password).isCompromised()?  → 400 COMPROMISED_PASSWORD
  4. passwordHash = bcrypt(password)          // {bcrypt}$2a$…
  5. roles = { CUSTOMER },  enabled = true
  6. save → 201 { id, publicId, email, enabled, roles, createdAt, updatedAt }
```

- The compromised-password check calls the HaveIBeenPwned range API (k-anonymity: only a
  SHA-1 prefix leaves the server). It runs **only here**, never on login.
- Code: `AuthService.register`.

### 5.2 Login — `POST /api/auth/login`

```
client → { email, password }
  1. AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
        → DaoAuthenticationProvider → AppUserDetailsService.loadUserByUsername(email)
        → bcrypt matches? account enabled?
  2. access  = JwtService.generateAccessToken(userId, email, roles)
  3. refresh = RefreshTokenService.issue(user)     // hashed + stored, raw returned
  4. response:
        body:       { accessToken, tokenType:"Bearer", expiresIn }
        Set-Cookie: refresh_token=<raw>; HttpOnly; Secure; SameSite=Strict; Path=/api/auth
        Set-Cookie: XSRF-TOKEN=<token>; Path=/        // for the double-submit CSRF check
```

Bad password / disabled account → `401 INVALID_CREDENTIALS` (never revealing which).

> **Implementation note.** The refresh cookie is written via
> `HttpServletResponse.addHeader(SET_COOKIE, …)`, **not** through the `ResponseEntity`
> headers. Spring writes the first `ResponseEntity` `Set-Cookie` with *replace*
> semantics, which would wipe the `XSRF-TOKEN` cookie the CSRF filter already set —
> leaving the client unable to make its first refresh call.

### 5.3 Authenticated request (bearer)

```
GET /api/... with  Authorization: Bearer <jwt>
  JwtAuthenticationFilter:
    no/again-malformed header → continue as anonymous (public paths still work)
    "Bearer <jwt>" → AuthenticationManager.authenticate(JwtAuthenticationToken.unauthenticated(jwt))
        → JwtAuthenticationProvider → JwtService.parse (sig + issuer + expiry)
        → authorities from `roles` claim, principal = userId
    success → SecurityContext populated → request proceeds
    failure → ProblemAuthenticationEntryPoint → 401 UNAUTHENTICATED
```

No database access on this path — the signed token is the source of truth.

### 5.4 Refresh — `POST /api/auth/refresh`  (cookie-authenticated, CSRF-protected)

```
browser sends: Cookie: refresh_token=<raw>; XSRF-TOKEN=<t>   +   Header: X-XSRF-TOKEN: <t>
  0. CSRF: X-XSRF-TOKEN header must equal the XSRF-TOKEN cookie  → else 403 ACCESS_DENIED
  1. no refresh cookie → 401
  2. RefreshTokenService.rotate(raw):
       hash = sha256(raw); row = findByTokenHash(hash)
       row missing            → 401 INVALID_REFRESH_TOKEN
       row.revoked == true    → REUSE: revokeAllForUser(userId); → 401 INVALID_REFRESH_TOKEN
       row.expiresAt past     → 401 INVALID_REFRESH_TOKEN
       else: row.revoked = true; issue new refresh; return { user, newRaw }
  3. new access token + rotated refresh cookie → 200
```

> **Transaction note.** Reuse detection must *persist* the family revocation even though
> the request is then rejected by throwing. Both `RefreshTokenService.rotate` and
> `AuthService.refresh` are annotated `@Transactional(noRollbackFor =
> InvalidRefreshTokenException.class)` so the throw does not roll back the revocation.

### 5.5 Logout — `POST /api/auth/logout`  (CSRF-protected)

```
  1. revoke the presented refresh token (idempotent; no error if already gone)
  2. Set-Cookie: refresh_token=; Max-Age=0    (clear the cookie)
  3. 204 No Content
```

The access token is **not** server-revoked (it's stateless); it simply expires within
its short TTL. Logout kills the refresh token so no new access tokens can be minted.

---

## 6. Authorization

### 6.1 Public vs private — `SecurityPaths`

Everything not listed is authenticated. Paths include the `/api` prefix added by
`WebMvcConfig`.

```
PUBLIC (permitAll):
  POST /api/auth/register, /api/auth/login, /api/auth/refresh, /api/auth/logout
  GET  /api/events/**, /api/venues/**, /api/version
       /error, GET /actuator/health
AUTHENTICATED: everything else
```

`/api/auth/refresh` and `/api/auth/logout` are `permitAll` at the authorization layer —
they are authenticated by the **refresh cookie** inside the service and guarded by CSRF,
not by a bearer token.

### 6.2 Roles & method security

- Roles are seeded in `roles`: `CUSTOMER`, `ORGANIZER`, `ADMIN` (V2 migration).
- A user's roles map to Spring authorities `ROLE_<name>` (`AppUserPrincipal`).
- Role names travel in the JWT `roles` claim, so bearer requests carry authorities with
  no DB lookup.
- `@EnableMethodSecurity` is on. Tighten any endpoint incrementally:

  ```java
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public void remove(@PathVariable Long id) { … }
  ```

- The current authenticated principal id is available via
  `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`
  (`AppUserPrincipal` on the login path, a bare `Long` user id on the bearer path).

---

## 7. CSRF

The API is a hybrid: **bearer** for normal calls, **cookie** for refresh/logout. Bearer
endpoints are inherently CSRF-safe (an attacker cannot set the `Authorization` header
cross-site), so CSRF is required **only** where the browser attaches the refresh cookie
automatically.

```java
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // XSRF-TOKEN cookie
    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())     // plain double-submit
    .requireCsrfProtectionMatcher(anyOf(                                 // ONLY these two
        POST "/api/auth/refresh", POST "/api/auth/logout")));
http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);           // emit the cookie
```

- **Double-submit:** the SPA reads the non-httpOnly `XSRF-TOKEN` cookie and echoes it in
  the `X-XSRF-TOKEN` header. The server compares header to cookie.
- `SameSite=Strict` on the refresh cookie is defense-in-depth (it is not sent on
  cross-site requests at all).
- All other state-changing endpoints (`POST /api/bookings`, …) need **no** CSRF token —
  the bearer header is the defense.
- `CsrfCookieFilter` forces the deferred token to resolve so the `XSRF-TOKEN` cookie is
  actually written to responses.

---

## 8. CORS

`SecurityConfig.corsConfigurationSource`:

- `allowedOrigins` = `app.cors.allowed-origins` (explicit list — **required** because…)
- `allowCredentials = true` (the browser must send the refresh cookie) ⇒ origins may not
  be `*`.
- `allowedMethods` = GET, POST, PUT, PATCH, DELETE, OPTIONS.
- `allowedHeaders` = `Authorization`, `Content-Type`, `X-XSRF-TOKEN`.

---

## 9. Error responses (RFC 9457)

Every failure is `application/problem+json` with a stable `code` and a `timestamp`,
consistent with `GlobalExceptionHandler`.

| Condition | Status | `code` | Source |
|-----------|--------|--------|--------|
| Unauthenticated / bad or missing token | 401 | `UNAUTHENTICATED` | `ProblemAuthenticationEntryPoint` |
| Authenticated but forbidden | 403 | `ACCESS_DENIED` | `ProblemAccessDeniedHandler` |
| Bad credentials / disabled at login | 401 | `INVALID_CREDENTIALS` | `GlobalExceptionHandler` |
| Refresh token invalid / expired / reused | 401 | `INVALID_REFRESH_TOKEN` | `GlobalExceptionHandler` |
| Registration password in a breach | 400 | `COMPROMISED_PASSWORD` | `GlobalExceptionHandler` |
| Duplicate email | 409 | `DUPLICATE_RESOURCE` | `GlobalExceptionHandler` |
| Bean-validation failure | 400 | `VALIDATION_ERROR` (+`errors[]`) | `GlobalExceptionHandler` |

`CompromisedPasswordException extends AuthenticationException`; the more specific 400
handler is declared, so compromised passwords map to 400 while all other auth failures
map to 401.

---

## 10. Auditing integration

`common/config/AuditingConfig` supplies the current actor id to Spring Data JPA
auditing (`@CreatedBy` / `@LastModifiedBy` on `AuditableEntity`):

```java
AuditorAware<Long> = () -> {
   auth = SecurityContextHolder…getAuthentication();
   anonymous/none → Optional.empty();                    // system writes leave columns null
   principal instanceof AppUserPrincipal p → p.getUserId();
   principal instanceof Long id → id;                    // bearer path
};
```

So `created_by` / `updated_by` on audited tables now record **who** performed each write.

---

## 11. Configuration

`application.properties` (secrets via environment):

```properties
app.jwt.secret=${JWT_SECRET}            # Base64, decodes to >= 256 bits — REQUIRED
app.jwt.issuer=eventhub
app.jwt.access-ttl=PT15M
app.jwt.refresh-ttl=P30D
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}
app.security.cookie.secure=${COOKIE_SECURE:true}
app.security.cookie.refresh-name=refresh_token
```

Required environment variables at runtime: `JWT_SECRET`, `DATABASE_URL`,
`DATABASE_USERNAME`, `DATABASE_PASSWORD`. For local HTTP dev also set
`COOKIE_SECURE=false` (a `Secure` cookie is dropped by clients over plain HTTP).

Generate a secret:

```bash
openssl rand -base64 32
```

> **Do not** set a fallback default for `app.jwt.secret`. A short/placeholder default
> (e.g. `${JWT_SECRET:secretkey}`) silently boots with an invalid signing key and fails
> deep in bean creation with a confusing `WeakKeyException`. Leaving it as `${JWT_SECRET}`
> makes a missing secret fail loudly.

No database migration is required for authentication — `users`, `roles`, `user_roles`,
and `refresh_tokens` already exist (V1); roles are seeded (V2).

---

## 12. Extending

- **Protect an endpoint by role:** add `@PreAuthorize("hasRole('ORGANIZER')")` (method
  security is enabled). No filter-chain edits needed.
- **Change the public surface:** edit the single `SecurityPaths.PUBLIC` array.
- **Add a role:** add to the `RoleName` enum + seed it; it flows through automatically.
- **Rotate the signing key:** change `JWT_SECRET`; existing access tokens become invalid
  (≤ 15 min window), refresh tokens keep working (they are opaque, not signed).
- **Email verification (future):** register currently sets `enabled=true`; switch to
  `false` and add a verification step — the `enabled` flag is already enforced at login
  by `DaoAuthenticationProvider`.

---

## 13. Verification

**Unit / slice tests** (`./mvnw test`): `JwtServiceTest` (round-trip, expiry, tamper,
issuer), `RefreshTokenServiceTest` (issue/rotate/expire/reuse/revoke),
`GlobalExceptionHandlerTest` (error mapping incl. 401/400 auth cases).

**Live end-to-end** (register → login → bearer → refresh → logout, plus negatives):
public browse 200, register 201, breached password 400, login 200 (both cookies),
bearer 200, no-token 401, refresh-without-CSRF 403, refresh-with-CSRF 200 (rotates),
logout 204; reuse: replay of a rotated token 401 and the whole family revoked.

The **Postman "EventHub API" → Auth** folder reproduces this flow (reads `XSRF-TOKEN`
from the cookie jar for the refresh/logout CSRF header). Run it top-to-bottom against a
running server with the `EventHub Local` environment.

---

## 14. Security notes

- Passwords: bcrypt via `DelegatingPasswordEncoder` (`{bcrypt}` prefix; upgradeable).
- Refresh tokens: never stored in plaintext; rotated; reuse triggers family revocation.
- Access tokens: short-lived, stateless, HS256 with a ≥256-bit key.
- Cookies: refresh cookie is `HttpOnly` (no JS access) + `SameSite=Strict`; the
  `XSRF-TOKEN` cookie is intentionally readable by JS (double-submit).
- CSRF only where it matters (cookie-authenticated endpoints); bearer endpoints rely on
  the un-forgeable `Authorization` header.
- `open-in-view=false`: user roles are fetched eagerly (`@EntityGraph`) for the
  `UserDetailsService`, so no lazy-loading outside a transaction.
