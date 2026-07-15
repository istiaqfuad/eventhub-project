package org.istiaqfuad.eventhub.user.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Read view of a user. {@code passwordHash} is never exposed; roles are
 * flattened to their names.
 */
public record UserResponse(
        Long id,
        UUID publicId,
        String email,
        boolean enabled,
        Set<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
