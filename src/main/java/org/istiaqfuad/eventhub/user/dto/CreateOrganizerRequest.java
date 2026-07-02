package org.istiaqfuad.eventhub.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Promote an existing user to an organizer. {@code verified} defaults to
 * false and is toggled by an admin, so it is not part of the request.
 */
public record CreateOrganizerRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "organization name is required")
        @Size(max = 255, message = "organization name must not exceed 255 characters")
        String orgName
) {
}
