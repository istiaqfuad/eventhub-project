package org.istiaqfuad.eventhub.payment.dto;

import org.istiaqfuad.eventhub.payment.entity.RefundStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RefundResponse(
        Long id,
        Long paymentId,
        BigDecimal amount,
        RefundStatus status,
        String reason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
