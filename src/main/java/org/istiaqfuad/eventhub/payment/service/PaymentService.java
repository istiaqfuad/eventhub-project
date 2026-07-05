package org.istiaqfuad.eventhub.payment.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.booking.service.BookingInventoryService;
import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.payment.PaymentGateway;
import org.istiaqfuad.eventhub.payment.StripeProperties;
import org.istiaqfuad.eventhub.payment.dto.PaymentRequest;
import org.istiaqfuad.eventhub.payment.dto.PaymentResponse;
import org.istiaqfuad.eventhub.payment.entity.Payment;
import org.istiaqfuad.eventhub.payment.entity.PaymentStatus;
import org.istiaqfuad.eventhub.payment.repository.PaymentRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Drives the payment lifecycle over a {@link PaymentGateway}. Initiation charges the booking's
 * server-side total via a hosted checkout and extends the hold to cover the payment window;
 * confirmation happens only from a verified webhook, which transitions the booking through
 * {@link BookingInventoryService}. Webhook handling is idempotent: state guards make duplicate
 * or out-of-order events no-ops.
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final BookingRepository bookings;
    private final PaymentGateway gateway;
    private final BookingInventoryService inventory;
    private final StripeProperties stripeProperties;

    public PaymentService(PaymentRepository payments, BookingRepository bookings, PaymentGateway gateway,
                          BookingInventoryService inventory, StripeProperties stripeProperties) {
        this.payments = payments;
        this.bookings = bookings;
        this.gateway = gateway;
        this.inventory = inventory;
        this.stripeProperties = stripeProperties;
    }

    public PaymentResponse create(PaymentRequest request, AuthenticatedUser caller) {
        Booking booking = bookings.findById(request.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.bookingId()));
        if (!caller.isAdmin() && !booking.getUser().getId().equals(caller.id())) {
            throw new AccessDeniedException("Booking does not belong to the caller");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Booking " + booking.getId() + " is not payable (status " + booking.getStatus() + ")");
        }

        long amountMinor = booking.getTotal().movePointRight(2).longValueExact();
        PaymentGateway.CheckoutRef ref = gateway.createCheckout(booking, amountMinor,
                stripeProperties.currency(), stripeProperties.checkoutExpiry(), request.idempotencyKey());

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotal());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProviderRef(ref.sessionId());
        payment.setIdempotencyKey(request.idempotencyKey());

        // Extend the hold so it can't be swept while the buyer is on the hosted checkout page.
        booking.setExpiresAt(OffsetDateTime.now().plus(stripeProperties.checkoutExpiry()));

        return toResponse(payments.save(payment), ref.url());
    }

    /** Applies a verified provider webhook. Idempotent via payment/booking state guards. */
    public void handleEvent(String payload, String signature) {
        PaymentGateway.PaymentEvent event = gateway.parseEvent(payload, signature);
        if (event.type() == PaymentGateway.PaymentEvent.Type.IGNORED) {
            return;
        }

        Booking booking = bookings.findById(event.bookingId()).orElse(null);
        if (booking == null) {
            log.warn("Webhook for unknown booking {} (session {})", event.bookingId(), event.sessionId());
            return;
        }
        Payment payment = payments.findByProviderRef(event.sessionId()).orElse(null);

        switch (event.type()) {
            case COMPLETED -> {
                if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                }
                if (booking.getStatus() == BookingStatus.PENDING) {
                    inventory.confirm(booking);
                }
            }
            case EXPIRED, FAILED -> {
                if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                    payment.setStatus(PaymentStatus.FAILED);
                }
                if (booking.getStatus() == BookingStatus.PENDING) {
                    inventory.release(booking);
                }
            }
            default -> { /* unreachable: IGNORED returned above */ }
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Long id, AuthenticatedUser caller) {
        Payment payment = payments.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        if (!caller.isAdmin() && !payment.getBooking().getUser().getId().equals(caller.id())) {
            throw new AccessDeniedException("Payment does not belong to the caller");
        }
        return toResponse(payment, null);
    }

    private PaymentResponse toResponse(Payment payment, String checkoutUrl) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getProviderRef(),
                checkoutUrl,
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
