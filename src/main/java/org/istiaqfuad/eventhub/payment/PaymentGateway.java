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

    /**
     * Refunds a payment. {@code providerRef} is the session id from checkout.
     * {@code amountMinor} is the amount to refund in the smallest unit.
     */
    RefundRef refund(String providerRef, long amountMinor, String reason, String idempotencyKey);

    /** A created refund: the provider refund id. */
    record RefundRef(String refundId) {
    }

    /** A verified webhook, reduced to what the domain needs. */
    record PaymentEvent(Type type, String providerRef, Long bookingId) {
        public enum Type {
            /** Payment succeeded — confirm the booking. */
            COMPLETED,
            /** Session expired — release the hold. */
            EXPIRED,
            /** Payment failed — release the hold. */
            FAILED,
            /** Refund succeeded. */
            REFUND_COMPLETED,
            /** Refund failed. */
            REFUND_FAILED,
            /** An event we don't act on. */
            IGNORED
        }
    }
}
