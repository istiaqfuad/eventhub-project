package org.istiaqfuad.eventhub.booking.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.booking.dto.BookingRequest;
import org.istiaqfuad.eventhub.booking.dto.BookingResponse;
import org.istiaqfuad.eventhub.booking.service.BookingService;
import org.istiaqfuad.eventhub.security.web.CurrentUserId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/bookings", version = "1")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request,
                                  @CurrentUserId Long userId) {
        return bookingService.create(request, userId);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) {
        return bookingService.get(id);
    }
}
