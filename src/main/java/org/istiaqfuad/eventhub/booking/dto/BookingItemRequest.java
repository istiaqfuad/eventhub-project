package org.istiaqfuad.eventhub.booking.dto;

import jakarta.validation.constraints.AssertTrue;

/**
 * One line of a booking: a reserved seat (assigned seating) OR a
 * general-admission ticket type. Mirrors the DB CHECK
 * {@code seat_id IS NOT NULL OR ticket_type_id IS NOT NULL}. Price is
 * resolved server-side from the seat's section or the ticket type — never
 * accepted from the client.
 */
public record BookingItemRequest(
        Long seatId,
        Long ticketTypeId
) {
    @AssertTrue(message = "either seatId or ticketTypeId must be provided")
    private boolean isTargetPresent() {
        return seatId != null || ticketTypeId != null;
    }
}
