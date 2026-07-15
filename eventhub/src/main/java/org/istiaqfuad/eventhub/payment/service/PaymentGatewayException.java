package org.istiaqfuad.eventhub.payment.service;

/** The payment provider rejected or failed a call (network, API error). Maps to 502. */
public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
