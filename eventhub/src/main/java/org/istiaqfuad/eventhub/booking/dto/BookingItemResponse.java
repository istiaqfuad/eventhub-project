package org.istiaqfuad.eventhub.booking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingItemResponse(
        Long id,
        UUID publicId,
        Long seatId,
        Long ticketTypeId,
        BigDecimal price
) {
}
