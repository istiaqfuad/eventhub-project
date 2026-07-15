package org.istiaqfuad.eventhub.booking.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;

public record BookingResponse(
        Long id,
        UUID publicId,
        Long userId,
        Long eventId,
        BookingStatus status,
        BigDecimal total,
        List<BookingItemResponse> items,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
