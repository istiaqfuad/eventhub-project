package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.dto.BookingResponse;
import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.entity.User;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.access.AccessDeniedException;

/** Ownership enforcement on {@link BookingService#get}. */
class BookingServiceTest {

    private static final long OWNER_ID = 10L;

    private BookingRepository bookings;
    private BookingService service;

    @BeforeEach
    void setUp() {
        bookings = mock(BookingRepository.class);
        service = new BookingService(bookings, mock(UserRepository.class), mock(EventRepository.class));

        User owner = new User();
        owner.setId(OWNER_ID);
        Event event = new Event();
        event.setId(50L);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(owner);
        booking.setEvent(event);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotal(BigDecimal.ZERO);
        when(bookings.findById(1L)).thenReturn(Optional.of(booking));
    }

    private static AuthenticatedUser user(long id, String... roles) {
        return new AuthenticatedUser(id, Set.of(roles));
    }

    @Test
    void ownerCanReadOwnBooking() {
        BookingResponse res = service.get(1L, user(OWNER_ID));
        assertThat(res.userId()).isEqualTo(OWNER_ID);
    }

    @Test
    void otherUserIsDenied() {
        assertThatThrownBy(() -> service.get(1L, user(OWNER_ID + 1)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanReadAnyBooking() {
        BookingResponse res = service.get(1L, user(999L, "ADMIN"));
        assertThat(res.userId()).isEqualTo(OWNER_ID);
    }
}
