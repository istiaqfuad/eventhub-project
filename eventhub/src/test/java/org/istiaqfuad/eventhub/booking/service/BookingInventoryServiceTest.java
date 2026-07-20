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
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.istiaqfuad.eventhub.outbox.service.OutboxService;

class BookingInventoryServiceTest {

    private BookingItemRepository bookingItems;
    private TicketTypeRepository ticketTypes;
    private OutboxService outboxService;
    private StringRedisTemplate redis;
    private BookingInventoryService service;

    private Seat seat;
    private TicketType ticketType;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingItems = mock(BookingItemRepository.class);
        ticketTypes = mock(TicketTypeRepository.class);
        outboxService = mock(OutboxService.class);
        redis = mock(StringRedisTemplate.class);
        service = new BookingInventoryService(bookingItems, ticketTypes, outboxService, redis);

        booking = new Booking();
        booking.setId(5L);
        booking.setStatus(BookingStatus.PENDING);
        org.istiaqfuad.eventhub.user.entity.User user = new org.istiaqfuad.eventhub.user.entity.User();
        user.setId(100L);
        booking.setUser(user);

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
    void confirmBooksSeatsAndConfirmsBookingAndClearsRedisHold() {
        service.confirm(booking);

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getExpiresAt()).isNull();
        // GA quota stays counted on confirm.
        verifyNoInteractions(ticketTypes);
        verify(redis).delete("seat:hold:" + seat.getId());
    }

    @Test
    void releaseFreesSeatsReturnsQuotaDeletesItemsAndCancelsAndClearsRedisHold() {
        service.release(booking);

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.FREE);
        verify(ticketTypes).release(7L, 1);
        // Items are deleted so the released seat can be booked again (uq_booking_item_seat).
        verify(bookingItems).deleteAll(anyList());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getExpiresAt()).isNull();
        verify(redis).delete("seat:hold:" + seat.getId());
    }
}
