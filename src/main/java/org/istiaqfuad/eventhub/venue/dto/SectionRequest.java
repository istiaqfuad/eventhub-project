package org.istiaqfuad.eventhub.venue.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.istiaqfuad.eventhub.venue.entity.SeatType;

import java.math.BigDecimal;

public record SectionRequest(
        @NotNull(message = "venueId is required")
        Long venueId,

        @NotBlank(message = "section name is required")
        @Size(max = 120, message = "section name must not exceed 120 characters")
        String name,

        SeatType seatType,

        @PositiveOrZero(message = "base price must not be negative")
        @Digits(integer = 10, fraction = 2, message = "base price must have at most 10 integer digits and 2 decimal places")
        BigDecimal basePrice
) {
}
