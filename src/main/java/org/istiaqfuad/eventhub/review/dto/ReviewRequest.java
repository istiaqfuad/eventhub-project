package org.istiaqfuad.eventhub.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Submit a review for an event. The author is the authenticated principal;
 * one review per user per event is enforced by the DB unique constraint
 * {@code uq_review_event_user}.
 */
public record ReviewRequest(
        @NotNull(message = "eventId is required")
        Long eventId,

        @NotNull(message = "rating is required")
        @Min(value = 1, message = "rating must be at least 1")
        @Max(value = 5, message = "rating must be at most 5")
        Integer rating,

        @Size(max = 5_000, message = "review body must not exceed 5000 characters")
        String body
) {
}
