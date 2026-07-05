package org.istiaqfuad.eventhub.payment.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.payment.dto.PaymentRequest;
import org.istiaqfuad.eventhub.payment.dto.PaymentResponse;
import org.istiaqfuad.eventhub.payment.entity.Payment;
import org.istiaqfuad.eventhub.payment.entity.PaymentStatus;
import org.istiaqfuad.eventhub.payment.repository.PaymentRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Ownership enforcement on {@link PaymentService} create and read. */
class PaymentServiceTest {

    private static final long OWNER_ID = 10L;

    private PaymentRepository payments;
    private BookingRepository bookings;
    private PaymentService service;

    private Booking booking(long ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        Booking b = new Booking();
        b.setId(5L);
        b.setUser(owner);
        b.setStatus(BookingStatus.PENDING);
        b.setTotal(new BigDecimal("42.00"));
        return b;
    }

    @BeforeEach
    void setUp() {
        payments = mock(PaymentRepository.class);
        bookings = mock(BookingRepository.class);
        service = new PaymentService(payments, bookings);
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static AuthenticatedUser user(long id, String... roles) {
        return new AuthenticatedUser(id, Set.of(roles));
    }

    @Test
    void ownerCanPayOwnBooking() {
        when(bookings.findById(5L)).thenReturn(Optional.of(booking(OWNER_ID)));
        PaymentResponse res = service.create(new PaymentRequest(5L, "idem-1"), user(OWNER_ID));
        assertThat(res.amount()).isEqualByComparingTo("42.00");
    }

    @Test
    void payingAnotherUsersBookingIsDenied() {
        when(bookings.findById(5L)).thenReturn(Optional.of(booking(OWNER_ID)));
        assertThatThrownBy(() -> service.create(new PaymentRequest(5L, "idem-1"), user(OWNER_ID + 1)))
                .isInstanceOf(AccessDeniedException.class);
        verify(payments, never()).save(any());
    }

    @Test
    void ownerCanReadOwnPayment() {
        Payment p = new Payment();
        p.setBooking(booking(OWNER_ID));
        p.setAmount(new BigDecimal("42.00"));
        p.setStatus(PaymentStatus.PENDING);
        when(payments.findById(7L)).thenReturn(Optional.of(p));

        assertThat(service.get(7L, user(OWNER_ID)).amount()).isEqualByComparingTo("42.00");
    }

    @Test
    void readingAnotherUsersPaymentIsDenied() {
        Payment p = new Payment();
        p.setBooking(booking(OWNER_ID));
        p.setAmount(new BigDecimal("42.00"));
        p.setStatus(PaymentStatus.PENDING);
        when(payments.findById(7L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.get(7L, user(OWNER_ID + 1)))
                .isInstanceOf(AccessDeniedException.class);
    }
}
