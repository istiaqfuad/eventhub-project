package org.istiaqfuad.eventhub.booking.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.booking.dto.BookingItemResponse;
import org.istiaqfuad.eventhub.booking.dto.BookingRequest;
import org.istiaqfuad.eventhub.booking.dto.BookingResponse;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Stub controller for wiring/validation testing only. Returns canned
 * responses with no persistence; replace with service-backed logic later.
 */
@RestController
@RequestMapping(path = "/bookings", version = "1")
public class BookingController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        List<BookingItemResponse> items = request.items().stream()
                .map(i -> new BookingItemResponse(0L, UUID.randomUUID(),
                        i.seatId(), i.ticketTypeId(), BigDecimal.ZERO))
                .toList();
        return new BookingResponse(1L, UUID.randomUUID(), 1L, request.eventId(),
                BookingStatus.PENDING, BigDecimal.ZERO, items, now, now);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        return new BookingResponse(id, UUID.randomUUID(), 1L, 1L,
                BookingStatus.PENDING, BigDecimal.ZERO, List.of(), now, now);
    }
}
