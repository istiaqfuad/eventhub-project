package org.istiaqfuad.eventhub.event.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TicketTypeResponse(
        Long id,
        Long eventId,
        String name,
        BigDecimal price,
        Integer quota,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
