package org.istiaqfuad.eventhub.payment;

import com.stripe.StripeClient;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.payment.service.PaymentGatewayException;
import org.istiaqfuad.eventhub.payment.service.WebhookVerificationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Stripe Checkout implementation of {@link PaymentGateway}. Creates hosted Checkout Sessions
 * (dynamic payment methods — {@code payment_method_types} is intentionally omitted) and
 * verifies inbound webhooks with the signing secret.
 */
@Component
public class StripePaymentGateway implements PaymentGateway {

    private static final String META_BOOKING_ID = "bookingId";

    private final StripeClient client;
    private final StripeProperties properties;

    public StripePaymentGateway(StripeClient client, StripeProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public CheckoutRef createCheckout(Booking booking, long amountMinor, String currency,
                                      Duration expiry, String idempotencyKey) {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(properties.successUrl())
                .setCancelUrl(properties.cancelUrl())
                .setClientReferenceId(booking.getPublicId().toString())
                .putMetadata(META_BOOKING_ID, booking.getId().toString())
                .setExpiresAt(Instant.now().plus(expiry).getEpochSecond())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(amountMinor)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Booking " + booking.getPublicId())
                                        .build())
                                .build())
                        .build())
                .build();
        try {
            RequestOptions options = RequestOptions.builder().setIdempotencyKey(idempotencyKey).build();
            Session session = client.v1().checkout().sessions().create(params, options);
            return new CheckoutRef(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new PaymentGatewayException("Stripe checkout session creation failed", e);
        }
    }

    @Override
    public PaymentEvent parseEvent(String payload, String signature) {
        Event event;
        try {
            event = client.constructEvent(payload, signature, properties.webhookSecret());
        } catch (SignatureVerificationException e) {
            throw new WebhookVerificationException("Invalid Stripe webhook signature", e);
        }

        PaymentEvent.Type type = switch (event.getType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                    PaymentEvent.Type.COMPLETED;
            case "checkout.session.expired" -> PaymentEvent.Type.EXPIRED;
            case "checkout.session.async_payment_failed" -> PaymentEvent.Type.FAILED;
            default -> PaymentEvent.Type.IGNORED;
        };
        if (type == PaymentEvent.Type.IGNORED) {
            return new PaymentEvent(PaymentEvent.Type.IGNORED, null, null);
        }

        // getObject() is empty when the event's API version differs from the SDK's pinned
        // version; deserializeUnsafe() maps it anyway (we only read id + metadata).
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject object = deserializer.getObject().orElseGet(() -> {
            try {
                return deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                throw new PaymentGatewayException(
                        "Webhook payload could not be deserialized for " + event.getType(), e);
            }
        });
        Session session = (Session) object;
        Long bookingId = Long.valueOf(session.getMetadata().get(META_BOOKING_ID));
        return new PaymentEvent(type, session.getId(), bookingId);
    }
}
