package org.istiaqfuad.eventhub.user.dto;

import java.time.OffsetDateTime;

public record OrganizerResponse(
        Long id,
        Long userId,
        String orgName,
        boolean verified,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
