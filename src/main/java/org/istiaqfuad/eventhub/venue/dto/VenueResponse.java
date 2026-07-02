package org.istiaqfuad.eventhub.venue.dto;

import java.time.OffsetDateTime;
import org.istiaqfuad.eventhub.venue.entity.LayoutType;

public record VenueResponse(
        Long id,
        String name,
        LayoutType layoutType,
        String address,
        String city,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
