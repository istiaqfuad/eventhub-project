package org.istiaqfuad.eventhub.messaging.consumer;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.invoice.entity.Invoice;
import org.istiaqfuad.eventhub.invoice.repository.InvoiceRepository;
import org.istiaqfuad.eventhub.messaging.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
public class InvoiceConsumer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceConsumer.class);

    private final BookingRepository bookings;
    private final InvoiceRepository invoices;

    public InvoiceConsumer(BookingRepository bookings, InvoiceRepository invoices) {
        this.bookings = bookings;
        this.invoices = invoices;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_INVOICE)
    @Transactional
    public void generateInvoice(Map<String, Object> payload) {
        Number bookingIdNum = (Number) payload.get("bookingId");
        if (bookingIdNum == null) {
            log.warn("Received invoice generation event without bookingId");
            return;
        }

        Long bookingId = bookingIdNum.longValue();

        // Idempotency check
        if (invoices.findByBookingId(bookingId).isPresent()) {
            log.info("Invoice already generated for booking {}", bookingId);
            return;
        }

        Booking booking = bookings.findById(bookingId).orElse(null);
        if (booking == null) {
            log.warn("Booking {} not found for invoice generation", bookingId);
            return;
        }

        Invoice invoice = new Invoice();
        invoice.setBooking(booking);
        invoice.setUser(booking.getUser());
        invoice.setTotalAmount(booking.getTotal());
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        invoices.save(invoice);
        log.info("Generated invoice {} for booking {}", invoice.getInvoiceNumber(), bookingId);
    }
}
