package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.event.entity.TicketType;
import org.istiaqfuad.eventhub.event.repository.TicketTypeRepository;
import org.istiaqfuad.eventhub.venue.entity.Seat;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BookingInventoryServiceTest {

    private BookingItemRepository bookingItems;
    private TicketTypeRepository ticketTypes;
    private BookingInventoryService service;

    private Seat seat;
    private TicketType ticketType;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingItems = mock(BookingItemRepository.class);
        ticketTypes = mock(TicketTypeRepository.class);
        service = new BookingInventoryService(bookingItems, ticketTypes);

        booking = new Booking();
        booking.setId(5L);
        booking.setStatus(BookingStatus.PENDING);

        seat = new Seat();
        seat.setStatus(SeatStatus.HELD);
        BookingItem seated = new BookingItem();
        seated.setSeat(seat);

        ticketType = new TicketType();
        ticketType.setId(7L);
        BookingItem ga = new BookingItem();
        ga.setTicketType(ticketType);

        when(bookingItems.findByBookingId(5L)).thenReturn(List.of(seated, ga));
    }

    @Test
    void confirmBooksSeatsAndConfirmsBooking() {
        service.confirm(booking);

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getExpiresAt()).isNull();
        // GA quota stays counted on confirm.
        verifyNoInteractions(ticketTypes);
    }

    @Test
    void releaseFreesSeatsReturnsQuotaAndCancels() {
        service.release(booking);

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.FREE);
        verify(ticketTypes).release(7L, 1);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getExpiresAt()).isNull();
    }
}
