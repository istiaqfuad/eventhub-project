package org.istiaqfuad.eventhub.event.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.istiaqfuad.eventhub.event.entity.EventStatus;

public record EventResponse(
        Long id,
        UUID publicId,
        Long organizerId,
        String title,
        String description,
        Long categoryId,
        Long venueId,
        String city,
        Double latitude,
        Double longitude,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        EventStatus status,
        boolean highDemand,
        Set<String> imageUrls,
        Set<Long> tagIds,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
