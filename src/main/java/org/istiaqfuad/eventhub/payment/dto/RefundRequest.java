package org.istiaqfuad.eventhub.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RefundRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @Size(max = 512, message = "reason must not exceed 512 characters")
        String reason,

        @Size(max = 80, message = "idempotency key must not exceed 80 characters")
        String idempotencyKey
) {
}
