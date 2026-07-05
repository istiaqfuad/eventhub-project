package org.istiaqfuad.eventhub.payment.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.payment.dto.PaymentRequest;
import org.istiaqfuad.eventhub.payment.dto.PaymentResponse;
import org.istiaqfuad.eventhub.payment.entity.Payment;
import org.istiaqfuad.eventhub.payment.entity.PaymentStatus;
import org.istiaqfuad.eventhub.payment.repository.PaymentRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic payment initiation/read. The charge amount is taken from the booking
 * total (never trusted from the client); provider integration and real
 * idempotency handling come later.
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository payments;
    private final BookingRepository bookings;

    public PaymentService(PaymentRepository payments, BookingRepository bookings) {
        this.payments = payments;
        this.bookings = bookings;
    }

    public PaymentResponse create(PaymentRequest request, AuthenticatedUser caller) {
        Booking booking = bookings.findById(request.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.bookingId()));
        if (!caller.isAdmin() && !booking.getUser().getId().equals(caller.id())) {
            throw new AccessDeniedException("Booking does not belong to the caller");
        }
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotal());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey(request.idempotencyKey());
        return toResponse(payments.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Long id, AuthenticatedUser caller) {
        Payment payment = payments.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        if (!caller.isAdmin() && !payment.getBooking().getUser().getId().equals(caller.id())) {
            throw new AccessDeniedException("Payment does not belong to the caller");
        }
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getProviderRef(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
