package org.istiaqfuad.eventhub.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-endpoint rate-limit thresholds, one minute window.
 * Externalized via environment variables so they can be tuned in production.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        int loginPerMinute,
        int bookingPerMinute,
        int paymentPerMinute
) {
    public RateLimitProperties {
        if (loginPerMinute <= 0) loginPerMinute = 10;
        if (bookingPerMinute <= 0) bookingPerMinute = 20;
        if (paymentPerMinute <= 0) paymentPerMinute = 10;
    }
}
