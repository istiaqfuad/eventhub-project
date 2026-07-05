package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.event.entity.TicketType;
import org.istiaqfuad.eventhub.event.repository.TicketTypeRepository;
import org.istiaqfuad.eventhub.venue.entity.Seat;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingExpiryServiceTest {

    private BookingRepository bookings;
    private BookingItemRepository bookingItems;
    private TicketTypeRepository ticketTypes;
    private BookingExpiryService service;

    @BeforeEach
    void setUp() {
        bookings = mock(BookingRepository.class);
        bookingItems = mock(BookingItemRepository.class);
        ticketTypes = mock(TicketTypeRepository.class);
        service = new BookingExpiryService(bookings, bookingItems, ticketTypes);
    }

    @Test
    void releasesSeatsAndGaThenCancels() {
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setStatus(BookingStatus.PENDING);

        Seat seat = new Seat();
        seat.setStatus(SeatStatus.HELD);
        BookingItem seated = new BookingItem();
        seated.setSeat(seat);

        TicketType tt = new TicketType();
        tt.setId(7L);
        BookingItem ga = new BookingItem();
        ga.setTicketType(tt);

        when(bookings.findExpiredPending(any(), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingItems.findByBookingId(5L)).thenReturn(List.of(seated, ga));

        int released = service.sweep();

        assertThat(released).isEqualTo(1);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.FREE);
        verify(ticketTypes).release(7L, 1);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getExpiresAt()).isNull();
    }

    @Test
    void noExpiredBookingsReleasesNothing() {
        when(bookings.findExpiredPending(any(), any(Pageable.class))).thenReturn(List.of());
        assertThat(service.sweep()).isZero();
    }
}
