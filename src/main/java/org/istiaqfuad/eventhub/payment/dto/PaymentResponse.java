package org.istiaqfuad.eventhub.payment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.istiaqfuad.eventhub.payment.entity.PaymentStatus;

public record PaymentResponse(
        Long id,
        Long bookingId,
        BigDecimal amount,
        PaymentStatus status,
        String providerRef,
        String checkoutUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
