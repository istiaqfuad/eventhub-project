package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.BookingProperties;
import org.istiaqfuad.eventhub.booking.dto.BookingItemRequest;
import org.istiaqfuad.eventhub.booking.dto.BookingItemResponse;
import org.istiaqfuad.eventhub.booking.dto.BookingRequest;
import org.istiaqfuad.eventhub.booking.dto.BookingResponse;
import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.entity.TicketType;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.event.repository.TicketTypeRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.istiaqfuad.eventhub.venue.entity.Seat;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;
import org.istiaqfuad.eventhub.venue.repository.SeatRepository;
import org.istiaqfuad.eventhub.waitingroom.WaitingRoomService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reserves a booking's line items all-or-nothing in one transaction: assigned
 * seats move FREE→HELD (optimistic {@code @Version} + the seat unique constraint
 * guard against double-sell); general-admission consumes quota via an atomic
 * conditional UPDATE. Prices come from the seat's section or the ticket type,
 * never the client. The booking is left PENDING with a hold that the
 * {@link BookingExpiryService} releases if payment never completes.
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookings;
    private final UserRepository users;
    private final EventRepository events;
    private final SeatRepository seats;
    private final TicketTypeRepository ticketTypes;
    private final BookingItemRepository bookingItems;
    private final BookingProperties bookingProperties;
    private final WaitingRoomService waitingRoom;
    private final StringRedisTemplate redis;

    public BookingService(BookingRepository bookings, UserRepository users, EventRepository events,
                          SeatRepository seats, TicketTypeRepository ticketTypes,
                          BookingItemRepository bookingItems, BookingProperties bookingProperties,
                          WaitingRoomService waitingRoom, StringRedisTemplate redis) {
        this.bookings = bookings;
        this.users = users;
        this.events = events;
        this.seats = seats;
        this.ticketTypes = ticketTypes;
        this.bookingItems = bookingItems;
        this.bookingProperties = bookingProperties;
        this.waitingRoom = waitingRoom;
        this.redis = redis;
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public BookingResponse create(BookingRequest request, Long userId) {
        Event event = events.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.eventId()));

        // High-demand events require a valid admission token from the waiting room
        if (Boolean.TRUE.equals(event.getHighDemand())
                && !waitingRoom.hasAdmissionToken(event.getId(), userId)) {
            throw new AccessDeniedException(
                    "A waiting-room admission token is required to book this event");
        }
        Booking booking = new Booking();
        booking.setUser(users.getReferenceById(userId));
        booking.setEvent(event);
        booking.setStatus(BookingStatus.PENDING);
        booking.setExpiresAt(OffsetDateTime.now().plus(bookingProperties.holdTtl()));

        List<BookingItem> items = new ArrayList<>();
        Set<Long> seenSeatIds = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (BookingItemRequest line : request.items()) {
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            if (line.seatId() != null) {
                item.setPrice(holdSeat(line.seatId(), event, seenSeatIds, item, userId));
            } else {
                item.setPrice(reserveGa(line.ticketTypeId(), request.eventId(), item));
            }
            total = total.add(item.getPrice());
            items.add(item);
        }

        booking.setTotal(total);
        Booking saved = bookings.save(booking);
        List<BookingItem> savedItems = bookingItems.saveAll(items);
        return toResponse(saved, savedItems);
    }

    private BigDecimal holdSeat(Long seatId, Event event, Set<Long> seenSeatIds, BookingItem item, Long userId) {
        if (!seenSeatIds.add(seatId)) {
            throw new InvalidReservationException("Duplicate seat in request: " + seatId);
        }
        
        String lockKey = "seat:hold:" + seatId;
        Boolean acquired = redis.opsForValue().setIfAbsent(lockKey, String.valueOf(userId), bookingProperties.holdTtl());
        if (Boolean.FALSE.equals(acquired)) {
            String owner = redis.opsForValue().get(lockKey);
            if (!String.valueOf(userId).equals(owner)) {
                throw new ReservationConflictException("Seat " + seatId + " is no longer available");
            }
        }
        Seat seat = seats.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId));
        if (event.getVenue() == null
                || !seat.getSection().getVenue().getId().equals(event.getVenue().getId())) {
            throw new InvalidReservationException("Seat " + seatId + " is not in the event's venue");
        }
        if (seat.getStatus() != SeatStatus.FREE) {
            throw new ReservationConflictException("Seat " + seatId + " is no longer available");
        }
        seat.setStatus(SeatStatus.HELD);
        item.setSeat(seat);
        return seat.getSection().getBasePrice();
    }

    private BigDecimal reserveGa(Long ticketTypeId, Long eventId, BookingItem item) {
        TicketType ticketType = ticketTypes.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", ticketTypeId));
        if (!ticketType.getEvent().getId().equals(eventId)) {
            throw new InvalidReservationException("Ticket type " + ticketTypeId + " is not for this event");
        }
        if (ticketTypes.reserve(ticketTypeId, 1) == 0) {
            throw new ReservationConflictException("Ticket type " + ticketTypeId + " is sold out");
        }
        item.setTicketType(ticketType);
        return ticketType.getPrice();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id, AuthenticatedUser caller) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        if (!caller.isAdmin() && !booking.getUser().getId().equals(caller.id())) {
            throw new AccessDeniedException("Booking does not belong to the caller");
        }
        return toResponse(booking, bookingItems.findByBookingId(booking.getId()));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listForUser(Long userId) {
        return bookings.findByUserId(userId).stream()
                .map(b -> toResponse(b, bookingItems.findByBookingId(b.getId())))
                .toList();
    }

    private BookingResponse toResponse(Booking booking, List<BookingItem> items) {
        List<BookingItemResponse> itemResponses = items.stream()
                .map(i -> new BookingItemResponse(
                        i.getId(),
                        i.getPublicId(),
                        i.getSeat() == null ? null : i.getSeat().getId(),
                        i.getTicketType() == null ? null : i.getTicketType().getId(),
                        i.getPrice()))
                .toList();
        return new BookingResponse(
                booking.getId(),
                booking.getPublicId(),
                booking.getUser().getId(),
                booking.getEvent().getId(),
                booking.getStatus(),
                booking.getTotal(),
                itemResponses,
                booking.getCreatedAt(),
                booking.getUpdatedAt());
    }
}
