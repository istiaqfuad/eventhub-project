package org.istiaqfuad.eventhub.payment;

import org.istiaqfuad.eventhub.booking.entity.Booking;

import java.time.Duration;

/**
 * Provider-agnostic payment seam. The reservation/confirm logic depends on this, not on
 * Stripe, so a different provider (e.g. a local aggregator for BD) can be swapped behind it.
 */
public interface PaymentGateway {

    /**
     * Starts a hosted checkout for the booking's amount and returns where to send the buyer.
     * {@code amountMinor} is the charge in the currency's smallest unit (never from the client).
     */
    CheckoutRef createCheckout(Booking booking, long amountMinor, String currency,
                               Duration expiry, String idempotencyKey);

    /** Verifies and interprets an inbound provider webhook. Throws if the signature is invalid. */
    PaymentEvent parseEvent(String payload, String signature);

    /** A created checkout: the provider session id (stored as the payment's provider ref) and the redirect URL. */
    record CheckoutRef(String sessionId, String url) {
    }

    /** A verified webhook, reduced to what the domain needs. */
    record PaymentEvent(Type type, String sessionId, Long bookingId) {
        public enum Type {
            /** Payment succeeded — confirm the booking. */
            COMPLETED,
            /** Session expired — release the hold. */
            EXPIRED,
            /** Payment failed — release the hold. */
            FAILED,
            /** An event we don't act on. */
            IGNORED
        }
    }
}
