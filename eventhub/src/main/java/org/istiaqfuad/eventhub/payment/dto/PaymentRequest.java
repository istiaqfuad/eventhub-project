package org.istiaqfuad.eventhub.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Initiate payment for a booking. {@code amount} is derived server-side from
 * the booking total (never trusted from the client). {@code idempotencyKey}
 * de-duplicates retried charge attempts.
 */
public record PaymentRequest(
        @NotNull(message = "bookingId is required")
        Long bookingId,

        @Size(max = 80, message = "idempotency key must not exceed 80 characters")
        String idempotencyKey
) {
}
