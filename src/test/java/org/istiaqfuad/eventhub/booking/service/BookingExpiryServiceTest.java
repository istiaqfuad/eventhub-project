package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BookingExpiryServiceTest {

    private BookingRepository bookings;
    private BookingInventoryService inventory;
    private BookingExpiryService service;

    @BeforeEach
    void setUp() {
        bookings = mock(BookingRepository.class);
        inventory = mock(BookingInventoryService.class);
        service = new BookingExpiryService(bookings, inventory);
    }

    @Test
    void releasesEachExpiredBooking() {
        Booking a = new Booking();
        a.setId(1L);
        a.setStatus(BookingStatus.PENDING);
        Booking b = new Booking();
        b.setId(2L);
        b.setStatus(BookingStatus.PENDING);
        when(bookings.findExpiredPending(any(), any(Pageable.class))).thenReturn(List.of(a, b));

        int released = service.sweep();

        assertThat(released).isEqualTo(2);
        verify(inventory).release(a);
        verify(inventory).release(b);
    }

    @Test
    void noExpiredBookingsReleasesNothing() {
        when(bookings.findExpiredPending(any(), any(Pageable.class))).thenReturn(List.of());
        assertThat(service.sweep()).isZero();
        verifyNoInteractions(inventory);
    }
}
