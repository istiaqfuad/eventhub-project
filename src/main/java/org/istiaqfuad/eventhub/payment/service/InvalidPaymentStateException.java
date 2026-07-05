package org.istiaqfuad.eventhub.payment.service;

/** The booking is not in a state that can be paid (e.g. not PENDING). Maps to 409. */
public class InvalidPaymentStateException extends RuntimeException {
    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
