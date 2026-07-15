package org.istiaqfuad.eventhub.review.dto;

import java.time.OffsetDateTime;

public record ReviewResponse(
        Long id,
        Long eventId,
        Long userId,
        Integer rating,
        String body,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
