package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.dto.BookingRequest;
import org.istiaqfuad.eventhub.booking.dto.BookingResponse;
import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Basic booking creation/read. Creates the booking header only — seat
 * locking, line-item persistence and total calculation are deferred to the
 * real reservation flow, so {@code items} is returned empty and {@code total}
 * is zero for now. {@code userId} comes from the authenticated caller.
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookings;
    private final UserRepository users;
    private final EventRepository events;

    public BookingService(BookingRepository bookings, UserRepository users, EventRepository events) {
        this.bookings = bookings;
        this.users = users;
        this.events = events;
    }

    public BookingResponse create(BookingRequest request, Long userId) {
        Booking booking = new Booking();
        booking.setUser(users.getReferenceById(userId));
        booking.setEvent(events.getReferenceById(request.eventId()));
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotal(BigDecimal.ZERO);
        return toResponse(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getPublicId(),
                booking.getUser().getId(),
                booking.getEvent().getId(),
                booking.getStatus(),
                booking.getTotal(),
                List.of(), // TODO: line items once seat reservation is implemented
                booking.getCreatedAt(),
                booking.getUpdatedAt());
    }
}
