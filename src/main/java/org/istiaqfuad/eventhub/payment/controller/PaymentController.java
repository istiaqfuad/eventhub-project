package org.istiaqfuad.eventhub.payment.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.payment.dto.PaymentRequest;
import org.istiaqfuad.eventhub.payment.dto.PaymentResponse;
import org.istiaqfuad.eventhub.payment.dto.RefundRequest;
import org.istiaqfuad.eventhub.payment.dto.RefundResponse;
import org.istiaqfuad.eventhub.payment.service.PaymentService;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.security.web.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/payments", version = "1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody PaymentRequest request,
                                  @CurrentUser AuthenticatedUser caller) {
        return paymentService.create(request, caller);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id, @CurrentUser AuthenticatedUser caller) {
        return paymentService.get(id, caller);
    }

    @PostMapping("/{id}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse refund(@PathVariable Long id,
                                 @Valid @RequestBody RefundRequest request,
                                 @CurrentUser AuthenticatedUser caller) {
        return paymentService.processRefund(id, request, caller);
    }

    /**
     * Stripe webhook. Public (no bearer) — authenticated by the signature inside the service,
     * verified against the raw body. Always 200 once handled so Stripe stops retrying; a bad
     * signature surfaces as 400 via the exception handler.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleEvent(payload, signature);
        return ResponseEntity.ok().build();
    }
}
