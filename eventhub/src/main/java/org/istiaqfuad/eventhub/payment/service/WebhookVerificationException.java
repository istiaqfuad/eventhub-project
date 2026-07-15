package org.istiaqfuad.eventhub.payment.service;

/** An inbound webhook failed signature verification (possibly spoofed). Maps to 400. */
public class WebhookVerificationException extends RuntimeException {
    public WebhookVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
