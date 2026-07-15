package org.istiaqfuad.eventhub.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT signing/lifetime configuration. {@code secret} must be Base64 and decode to
 * at least 256 bits (32 bytes) for HS256; jjwt rejects weaker keys.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTtl,
        Duration refreshTtl) {
}
