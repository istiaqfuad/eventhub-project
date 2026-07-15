package org.istiaqfuad.eventhub.event.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Event create/update input. {@code status} (defaults DRAFT), {@code highDemand}
 * and {@code publicId} are server-owned and excluded.
 */
public record EventRequest(
        // Optional: the owning organizer is derived from the authenticated caller.
        // Only honored when an ADMIN supplies it to act on another organizer's behalf.
        Long organizerId,

        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must not exceed 255 characters")
        String title,

        @Size(max = 10_000, message = "description must not exceed 10000 characters")
        String description,

        Long categoryId,

        Long venueId,

        @Size(max = 120, message = "city must not exceed 120 characters")
        String city,

        @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "latitude must be between -90 and 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "longitude must be between -180 and 180")
        Double longitude,

        @NotNull(message = "start time is required")
        @Future(message = "start time must be in the future")
        OffsetDateTime startsAt,

        @NotNull(message = "end time is required")
        @Future(message = "end time must be in the future")
        OffsetDateTime endsAt,

        Set<
                @NotBlank(message = "image URL must not be blank")
                @Size(max = 512, message = "image URL must not exceed 512 characters")
                String> imageUrls,

        Set<Long> tagIds
) {
    @AssertTrue(message = "endsAt must be after startsAt")
    private boolean isEndAfterStart() {
        return startsAt == null || endsAt == null || endsAt.isAfter(startsAt);
    }
}
