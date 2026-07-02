package org.istiaqfuad.eventhub.payment.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.payment.dto.PaymentRequest;
import org.istiaqfuad.eventhub.payment.dto.PaymentResponse;
import org.istiaqfuad.eventhub.payment.entity.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Stub controller for wiring/validation testing only. Returns canned
 * responses with no persistence; replace with service-backed logic later.
 */
@RestController
@RequestMapping(path = "/payments", version = "1")
public class PaymentController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody PaymentRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        return new PaymentResponse(1L, request.bookingId(), BigDecimal.ZERO,
                PaymentStatus.PENDING, null, now, now);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        return new PaymentResponse(id, 1L, BigDecimal.ZERO,
                PaymentStatus.PENDING, null, now, now);
    }
}
