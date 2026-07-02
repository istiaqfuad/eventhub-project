package org.istiaqfuad.eventhub.payment.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.payment.dto.PaymentRequest;
import org.istiaqfuad.eventhub.payment.dto.PaymentResponse;
import org.istiaqfuad.eventhub.payment.entity.Payment;
import org.istiaqfuad.eventhub.payment.entity.PaymentStatus;
import org.istiaqfuad.eventhub.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

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

    public PaymentResponse create(PaymentRequest request) {
        Booking booking = bookings.findById(request.bookingId())
                .orElseThrow(() -> new NoSuchElementException("booking not found: " + request.bookingId()));
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotal());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey(request.idempotencyKey());
        return toResponse(payments.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        Payment payment = payments.findById(id)
                .orElseThrow(() -> new NoSuchElementException("payment not found: " + id));
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
