package org.istiaqfuad.eventhub.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Stripe Checkout configuration. Secrets ({@code secretKey}, {@code webhookSecret}) come
 * from the environment and have no defaults, so the app fails fast if they are unset.
 * {@code checkoutExpiry} must be at least 30 minutes — Stripe's minimum session lifetime.
 */
@ConfigurationProperties(prefix = "app.stripe")
public record StripeProperties(
        String secretKey,
        String webhookSecret,
        String currency,
        String successUrl,
        String cancelUrl,
        Duration checkoutExpiry) {
}
