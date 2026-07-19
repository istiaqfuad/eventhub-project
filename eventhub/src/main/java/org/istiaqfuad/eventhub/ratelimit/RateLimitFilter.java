package org.istiaqfuad.eventhub.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Sliding-window rate limiter backed by a single atomic Redis Lua script.
 *
 * <p>Key: {@code rl:{endpoint}:{identifier}} where identifier is the authenticated
 * user's ID (stable across requests) or the client's IP address (fallback for anonymous).
 *
 * <p>The Lua script atomically:
 * <ol>
 *   <li>Trims entries older than the window boundary (ZREMRANGEBYSCORE)</li>
 *   <li>Counts remaining entries (ZCARD)</li>
 *   <li>Rejects if count ≥ limit</li>
 *   <li>Adds the current timestamp as a sorted-set member (ZADD)</li>
 *   <li>Sets TTL on the key so idle keys are cleaned up (EXPIRE)</li>
 * </ol>
 *
 * <p>All six operations run inside one Lua call — no race between trim and add.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    /** Window size in milliseconds. */
    private static final long WINDOW_MS = 60_000L;

    /**
     * Atomic sliding-window script.
     * KEYS[1] = rate-limit key
     * ARGV[1] = now (ms since epoch, as string)
     * ARGV[2] = window (ms, as string)
     * ARGV[3] = limit (max requests per window)
     * Returns 1 if allowed, 0 if rejected.
     */
    private static final RedisScript<Long> SCRIPT = RedisScript.of("""
            local key    = KEYS[1]
            local now    = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit  = tonumber(ARGV[3])
            redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
            local count = redis.call('ZCARD', key)
            if count >= limit then return 0 end
            redis.call('ZADD', key, now, now .. '-' .. redis.call('INCR', key .. ':seq'))
            redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)
            return 1
            """, Long.class);

    private final StringRedisTemplate redis;
    private final RateLimitProperties properties;

    public RateLimitFilter(StringRedisTemplate redis, RateLimitProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        EndpointGroup group = resolveGroup(path);
        if (group == null) {
            chain.doFilter(request, response);
            return;
        }

        String identifier = resolveIdentifier(request);
        String key = "rl:" + group.name().toLowerCase() + ":" + identifier;
        long now = Instant.now().toEpochMilli();
        int limit = limit(group);

        Long result = redis.execute(SCRIPT,
                List.of(key),
                String.valueOf(now),
                String.valueOf(WINDOW_MS),
                String.valueOf(limit));

        if (!Long.valueOf(1L).equals(result)) {
            log.warn("Rate limit exceeded: path={} identifier={}", path, identifier);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("""
                    {"type":"about:blank","title":"Too Many Requests",\
                    "status":429,"detail":"Rate limit exceeded. Try again later.",\
                    "code":"RATE_LIMIT_EXCEEDED"}""");
            return;
        }

        chain.doFilter(request, response);
    }

    private EndpointGroup resolveGroup(String path) {
        if (path.equals("/api/auth/login")) return EndpointGroup.LOGIN;
        if (path.startsWith("/api/bookings")) return EndpointGroup.BOOKING;
        if (path.startsWith("/api/payments") && !path.endsWith("/webhook")) return EndpointGroup.PAYMENT;
        return null;
    }

    private String resolveIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long userId) {
            return "user:" + userId;
        }
        // Fall back to IP for anonymous (login endpoint)
        String forwarded = request.getHeader("X-Forwarded-For");
        return "ip:" + (forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr());
    }

    private int limit(EndpointGroup group) {
        return switch (group) {
            case LOGIN -> properties.loginPerMinute();
            case BOOKING -> properties.bookingPerMinute();
            case PAYMENT -> properties.paymentPerMinute();
        };
    }

    private enum EndpointGroup {
        LOGIN, BOOKING, PAYMENT
    }
}
