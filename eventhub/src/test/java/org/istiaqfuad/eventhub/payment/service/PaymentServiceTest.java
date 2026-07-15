package org.istiaqfuad.eventhub.payment.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.booking.service.BookingInventoryService;
import org.istiaqfuad.eventhub.payment.PaymentGateway;
import org.istiaqfuad.eventhub.payment.StripeProperties;
import org.istiaqfuad.eventhub.payment.dto.PaymentRequest;
import org.istiaqfuad.eventhub.payment.dto.PaymentResponse;
import org.istiaqfuad.eventhub.payment.dto.RefundRequest;
import org.istiaqfuad.eventhub.payment.dto.RefundResponse;
import org.istiaqfuad.eventhub.payment.entity.Payment;
import org.istiaqfuad.eventhub.payment.entity.PaymentStatus;
import org.istiaqfuad.eventhub.payment.entity.Refund;
import org.istiaqfuad.eventhub.payment.entity.RefundStatus;
import org.istiaqfuad.eventhub.payment.repository.PaymentRepository;
import org.istiaqfuad.eventhub.payment.repository.RefundRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private static final long OWNER_ID = 10L;
    private static final long BOOKING_ID = 5L;
    private static final String SESSION_ID = "cs_test_1";

    private PaymentRepository payments;
    private RefundRepository refunds;
    private BookingRepository bookings;
    private PaymentGateway gateway;
    private BookingInventoryService inventory;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        payments = mock(PaymentRepository.class);
        refunds = mock(RefundRepository.class);
        bookings = mock(BookingRepository.class);
        gateway = mock(PaymentGateway.class);
        inventory = mock(BookingInventoryService.class);
        StripeProperties props = new StripeProperties(
                "sk", "whsec", "usd", "https://ok", "https://cancel", Duration.ofMinutes(30));
        service = new PaymentService(payments, refunds, bookings, gateway, inventory, props);
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refunds.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Booking booking(BookingStatus status) {
        User owner = new User();
        owner.setId(OWNER_ID);
        Booking b = new Booking();
        b.setId(BOOKING_ID);
        b.setUser(owner);
        b.setStatus(status);
        b.setTotal(new BigDecimal("50.00"));
        return b;
    }

    private static AuthenticatedUser user(long id, String... roles) {
        return new AuthenticatedUser(id, Set.of(roles));
    }

    @Test
    void initiateCreatesCheckoutAndPendingPayment() {
        Booking b = booking(BookingStatus.PENDING);
        when(bookings.findById(BOOKING_ID)).thenReturn(Optional.of(b));
        when(gateway.createCheckout(eq(b), eq(5000L), eq("usd"), any(), eq("idem-1")))
                .thenReturn(new PaymentGateway.CheckoutRef(SESSION_ID, "https://checkout.stripe/x"));

        PaymentResponse res = service.create(new PaymentRequest(BOOKING_ID, "idem-1"), user(OWNER_ID));

        assertThat(res.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(res.providerRef()).isEqualTo(SESSION_ID);
        assertThat(res.checkoutUrl()).isEqualTo("https://checkout.stripe/x");
        assertThat(res.amount()).isEqualByComparingTo("50.00");
        assertThat(b.getExpiresAt()).isNotNull();   // hold extended to cover the payment window
    }

    @Test
    void initiateOnNonPendingBookingIsRejected() {
        when(bookings.findById(BOOKING_ID)).thenReturn(Optional.of(booking(BookingStatus.CONFIRMED)));
        assertThatThrownBy(() -> service.create(new PaymentRequest(BOOKING_ID, "idem-1"), user(OWNER_ID)))
                .isInstanceOf(InvalidPaymentStateException.class);
    }

    @Test
    void initiateByNonOwnerIsDenied() {
        when(bookings.findById(BOOKING_ID)).thenReturn(Optional.of(booking(BookingStatus.PENDING)));
        assertThatThrownBy(() -> service.create(new PaymentRequest(BOOKING_ID, "idem-1"), user(OWNER_ID + 1)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void completedEventConfirmsBookingAndSucceedsPayment() {
        Booking b = booking(BookingStatus.PENDING);
        Payment p = new Payment();
        p.setBooking(b);
        p.setStatus(PaymentStatus.PENDING);
        when(gateway.parseEvent("body", "sig"))
                .thenReturn(new PaymentGateway.PaymentEvent(PaymentGateway.PaymentEvent.Type.COMPLETED, SESSION_ID, BOOKING_ID));
        when(bookings.findById(BOOKING_ID)).thenReturn(Optional.of(b));
        when(payments.findByProviderRef(SESSION_ID)).thenReturn(Optional.of(p));

        service.handleEvent("body", "sig");

        assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        verify(inventory).confirm(b);
    }

    @Test
    void duplicateCompletedIsNoOpWhenAlreadyConfirmed() {
        Booking b = booking(BookingStatus.CONFIRMED);
        Payment p = new Payment();
        p.setBooking(b);
        p.setStatus(PaymentStatus.SUCCEEDED);
        when(gateway.parseEvent("body", "sig"))
                .thenReturn(new PaymentGateway.PaymentEvent(PaymentGateway.PaymentEvent.Type.COMPLETED, SESSION_ID, BOOKING_ID));
        when(bookings.findById(BOOKING_ID)).thenReturn(Optional.of(b));
        when(payments.findByProviderRef(SESSION_ID)).thenReturn(Optional.of(p));

        service.handleEvent("body", "sig");

        verify(inventory, never()).confirm(any());
    }

    @Test
    void expiredEventFailsPaymentAndReleasesHold() {
        Booking b = booking(BookingStatus.PENDING);
        Payment p = new Payment();
        p.setBooking(b);
        p.setStatus(PaymentStatus.PENDING);
        when(gateway.parseEvent("body", "sig"))
                .thenReturn(new PaymentGateway.PaymentEvent(PaymentGateway.PaymentEvent.Type.EXPIRED, SESSION_ID, BOOKING_ID));
        when(bookings.findById(BOOKING_ID)).thenReturn(Optional.of(b));
        when(payments.findByProviderRef(SESSION_ID)).thenReturn(Optional.of(p));

        service.handleEvent("body", "sig");

        assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(inventory).release(b);
    }

    @Test
    void ignoredEventDoesNothing() {
        when(gateway.parseEvent("body", "sig"))
                .thenReturn(new PaymentGateway.PaymentEvent(PaymentGateway.PaymentEvent.Type.IGNORED, null, null));

        service.handleEvent("body", "sig");

        verify(inventory, never()).confirm(any());
        verify(inventory, never()).release(any());
    }

    @Test
    void processRefundSucceeds() {
        Booking b = booking(BookingStatus.CONFIRMED);
        Payment p = new Payment();
        p.setId(1L);
        p.setBooking(b);
        p.setAmount(new BigDecimal("50.00"));
        p.setStatus(PaymentStatus.SUCCEEDED);
        p.setProviderRef(SESSION_ID);
        
        when(payments.findById(1L)).thenReturn(Optional.of(p));
        when(refunds.findByPaymentId(1L)).thenReturn(java.util.Collections.emptyList());
        when(gateway.refund(eq(SESSION_ID), eq(2500L), eq("test reason"), eq("idem-2")))
            .thenReturn(new PaymentGateway.RefundRef("re_test_1"));

        RefundResponse res = service.processRefund(1L, new RefundRequest(new BigDecimal("25.00"), "test reason", "idem-2"), user(OWNER_ID));

        assertThat(res.amount()).isEqualByComparingTo("25.00");
        assertThat(res.status()).isEqualTo(RefundStatus.COMPLETED);
    }

    @Test
    void refundCompletedEventUpdatesRefundStatus() {
        Refund r = new Refund();
        r.setStatus(RefundStatus.REQUESTED);
        when(gateway.parseEvent("body", "sig"))
                .thenReturn(new PaymentGateway.PaymentEvent(PaymentGateway.PaymentEvent.Type.REFUND_COMPLETED, "re_1", null));
        when(refunds.findByProviderRef("re_1")).thenReturn(Optional.of(r));

        service.handleEvent("body", "sig");

        assertThat(r.getStatus()).isEqualTo(RefundStatus.COMPLETED);
    }

    @Test
    void refundFailedEventUpdatesRefundStatus() {
        Refund r = new Refund();
        r.setStatus(RefundStatus.REQUESTED);
        when(gateway.parseEvent("body", "sig"))
                .thenReturn(new PaymentGateway.PaymentEvent(PaymentGateway.PaymentEvent.Type.REFUND_FAILED, "re_1", null));
        when(refunds.findByProviderRef("re_1")).thenReturn(Optional.of(r));

        service.handleEvent("body", "sig");

        assertThat(r.getStatus()).isEqualTo(RefundStatus.FAILED);
    }
}
