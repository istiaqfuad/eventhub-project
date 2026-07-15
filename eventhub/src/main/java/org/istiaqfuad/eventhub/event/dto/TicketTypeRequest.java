package org.istiaqfuad.eventhub.event.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TicketTypeRequest(
        @NotNull(message = "eventId is required")
        Long eventId,

        @NotBlank(message = "ticket type name is required")
        @Size(max = 120, message = "ticket type name must not exceed 120 characters")
        String name,

        @NotNull(message = "price is required")
        @PositiveOrZero(message = "price must not be negative")
        @Digits(integer = 10, fraction = 2, message = "price must have at most 10 integer digits and 2 decimal places")
        BigDecimal price,

        @NotNull(message = "quota is required")
        @Positive(message = "quota must be positive")
        Integer quota
) {
}
