package org.istiaqfuad.eventhub.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Create a booking for an event. The buyer is taken from the authenticated
 * principal (not this payload); {@code status}, {@code total} and
 * {@code publicId} are server-owned.
 */
public record BookingRequest(
        @NotNull(message = "eventId is required")
        Long eventId,

        @NotEmpty(message = "booking must contain at least one item")
        List<@Valid BookingItemRequest> items
) {
}
