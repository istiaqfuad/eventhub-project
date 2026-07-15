package org.istiaqfuad.eventhub.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Seat creation input. {@code status} defaults to FREE and {@code version}
 * is managed by the optimistic-lock mechanism, so neither is client-supplied.
 */
public record SeatRequest(
        @NotNull(message = "sectionId is required")
        Long sectionId,

        @NotBlank(message = "row label is required")
        @Size(max = 10, message = "row label must not exceed 10 characters")
        String rowLabel,

        @NotNull(message = "column number is required")
        @Positive(message = "column number must be positive")
        Integer colNumber
) {
}
