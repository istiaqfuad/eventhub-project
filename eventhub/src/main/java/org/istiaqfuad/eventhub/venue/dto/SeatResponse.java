package org.istiaqfuad.eventhub.venue.dto;

import java.time.OffsetDateTime;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;

public record SeatResponse(
        Long id,
        Long sectionId,
        String rowLabel,
        Integer colNumber,
        SeatStatus status,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
