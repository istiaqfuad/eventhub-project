package org.istiaqfuad.eventhub.venue.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.istiaqfuad.eventhub.venue.entity.SeatType;

public record SectionResponse(
        Long id,
        Long venueId,
        String name,
        SeatType seatType,
        BigDecimal basePrice,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
