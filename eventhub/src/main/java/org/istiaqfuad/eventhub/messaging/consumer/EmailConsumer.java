package org.istiaqfuad.eventhub.messaging.consumer;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.messaging.RabbitConfig;
import org.istiaqfuad.eventhub.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final BookingRepository bookings;

    public EmailConsumer(BookingRepository bookings) {
        this.bookings = bookings;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_EMAIL)
    @Transactional(readOnly = true)
    public void sendBookingConfirmationEmail(Map<String, Object> payload) {
        Number bookingIdNum = (Number) payload.get("bookingId");
        if (bookingIdNum == null) {
            log.warn("Received email event without bookingId");
            return;
        }

        Long bookingId = bookingIdNum.longValue();

        Booking booking = bookings.findById(bookingId).orElse(null);
        if (booking == null) {
            log.warn("Booking {} not found for email generation", bookingId);
            return;
        }

        User user = booking.getUser();
        String eventTitle = booking.getEvent().getTitle();

        // Simulate sending an email
        log.info("==================================================");
        log.info("EMAIL SENT TO: {}", user.getEmail());
        log.info("SUBJECT: Booking Confirmation - {}", eventTitle);
        log.info("BODY:");
        log.info("Dear User ({}),", user.getEmail());
        log.info("Your booking (ID: {}) for '{}' has been confirmed.", booking.getPublicId(), eventTitle);
        log.info("Total paid: ${}", booking.getTotal());
        log.info("Thank you for using EventHub!");
        log.info("==================================================");
    }
}
