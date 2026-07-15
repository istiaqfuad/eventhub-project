package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.BookingProperties;
import org.istiaqfuad.eventhub.booking.dto.BookingItemRequest;
import org.istiaqfuad.eventhub.booking.dto.BookingRequest;
import org.istiaqfuad.eventhub.booking.dto.BookingResponse;
import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.entity.TicketType;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.event.repository.TicketTypeRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.entity.User;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.istiaqfuad.eventhub.venue.entity.Section;
import org.istiaqfuad.eventhub.venue.entity.Seat;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;
import org.istiaqfuad.eventhub.venue.entity.Venue;
import org.istiaqfuad.eventhub.venue.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    private static final long USER_ID = 10L;
    private static final long EVENT_ID = 1L;
    private static final long VENUE_ID = 100L;

    private BookingRepository bookings;
    private UserRepository users;
    private EventRepository events;
    private SeatRepository seats;
    private TicketTypeRepository ticketTypes;
    private BookingItemRepository bookingItems;
    private BookingService service;

    @BeforeEach
    void setUp() {
        bookings = mock(BookingRepository.class);
        users = mock(UserRepository.class);
        events = mock(EventRepository.class);
        seats = mock(SeatRepository.class);
        ticketTypes = mock(TicketTypeRepository.class);
        bookingItems = mock(BookingItemRepository.class);
        service = new BookingService(bookings, users, events, seats, ticketTypes, bookingItems,
                new BookingProperties(Duration.ofMinutes(15)));

        when(users.getReferenceById(USER_ID)).thenReturn(userRef());
        when(bookings.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingItems.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(events.findById(EVENT_ID)).thenReturn(Optional.of(event()));
    }

    private static User userRef() {
        User u = new User();
        u.setId(USER_ID);
        return u;
    }

    private static Venue venue() {
        Venue v = new Venue();
        v.setId(VENUE_ID);
        return v;
    }

    private static Event event() {
        Event e = new Event();
        e.setId(EVENT_ID);
        e.setVenue(venue());
        return e;
    }

    private Seat freeSeat(long id, String price) {
        Section section = new Section();
        section.setVenue(venue());
        section.setBasePrice(new BigDecimal(price));
        Seat seat = new Seat();
        seat.setId(id);
        seat.setSection(section);
        seat.setStatus(SeatStatus.FREE);
        when(seats.findById(id)).thenReturn(Optional.of(seat));
        return seat;
    }

    private TicketType ticketType(long id, String price) {
        TicketType tt = new TicketType();
        tt.setId(id);
        tt.setEvent(event());
        tt.setPrice(new BigDecimal(price));
        when(ticketTypes.findById(id)).thenReturn(Optional.of(tt));
        return tt;
    }

    private static BookingRequest seatedRequest(Long... seatIds) {
        return new BookingRequest(EVENT_ID, java.util.Arrays.stream(seatIds)
                .map(id -> new BookingItemRequest(id, null)).toList());
    }

    @Test
    void seatedBookingHoldsSeatsAndSumsSectionPrices() {
        Seat s1 = freeSeat(1L, "50.00");
        Seat s2 = freeSeat(2L, "30.00");

        BookingResponse res = service.create(seatedRequest(1L, 2L), USER_ID);

        assertThat(s1.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(s2.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(res.total()).isEqualByComparingTo("80.00");
        assertThat(res.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(res.items()).hasSize(2);
    }

    @Test
    void gaBookingReservesQuotaAndUsesTicketPrice() {
        ticketType(7L, "25.00");
        when(ticketTypes.reserve(eq(7L), anyInt())).thenReturn(1);

        BookingResponse res = service.create(
                new BookingRequest(EVENT_ID, List.of(new BookingItemRequest(null, 7L))), USER_ID);

        verify(ticketTypes).reserve(7L, 1);
        assertThat(res.total()).isEqualByComparingTo("25.00");
    }

    @Test
    void mixedBookingSumsSeatedAndGa() {
        freeSeat(1L, "50.00");
        ticketType(7L, "25.00");
        when(ticketTypes.reserve(eq(7L), anyInt())).thenReturn(1);

        BookingResponse res = service.create(new BookingRequest(EVENT_ID,
                List.of(new BookingItemRequest(1L, null), new BookingItemRequest(null, 7L))), USER_ID);

        assertThat(res.total()).isEqualByComparingTo("75.00");
    }

    @Test
    void heldSeatIsRejectedAsConflict() {
        Seat seat = freeSeat(1L, "50.00");
        seat.setStatus(SeatStatus.HELD);

        assertThatThrownBy(() -> service.create(seatedRequest(1L), USER_ID))
                .isInstanceOf(ReservationConflictException.class);
        verify(bookingItems, never()).saveAll(any());
    }

    @Test
    void gaSoldOutIsRejectedAsConflict() {
        ticketType(7L, "25.00");
        when(ticketTypes.reserve(eq(7L), anyInt())).thenReturn(0);

        assertThatThrownBy(() -> service.create(
                new BookingRequest(EVENT_ID, List.of(new BookingItemRequest(null, 7L))), USER_ID))
                .isInstanceOf(ReservationConflictException.class);
    }

    @Test
    void seatInAnotherVenueIsRejectedAsInvalid() {
        Section section = new Section();
        Venue other = new Venue();
        other.setId(999L);
        section.setVenue(other);
        section.setBasePrice(new BigDecimal("50.00"));
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSection(section);
        seat.setStatus(SeatStatus.FREE);
        when(seats.findById(1L)).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> service.create(seatedRequest(1L), USER_ID))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void duplicateSeatInRequestIsRejected() {
        freeSeat(1L, "50.00");
        assertThatThrownBy(() -> service.create(seatedRequest(1L, 1L), USER_ID))
                .isInstanceOf(InvalidReservationException.class);
    }

    // --- ownership on get (unchanged behavior, updated constructor) ---

    @Test
    void ownerCanReadOwnBooking() {
        User owner = new User();
        owner.setId(USER_ID);
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setUser(owner);
        booking.setEvent(event());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotal(BigDecimal.ZERO);
        when(bookings.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingItems.findByBookingId(5L)).thenReturn(List.of());

        assertThat(service.get(5L, new AuthenticatedUser(USER_ID, Set.of())).userId()).isEqualTo(USER_ID);
    }

    @Test
    void otherUserIsDeniedOnGet() {
        User owner = new User();
        owner.setId(USER_ID);
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setUser(owner);
        booking.setEvent(event());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotal(BigDecimal.ZERO);
        when(bookings.findById(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.get(5L, new AuthenticatedUser(USER_ID + 1, Set.of())))
                .isInstanceOf(AccessDeniedException.class);
    }
}
