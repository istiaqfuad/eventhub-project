package org.istiaqfuad.eventhub.messaging.consumer;

import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.messaging.RabbitConfig;
import org.istiaqfuad.eventhub.ticket.entity.Ticket;
import org.istiaqfuad.eventhub.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);

    private final BookingItemRepository bookingItems;
    private final TicketRepository tickets;

    public TicketConsumer(BookingItemRepository bookingItems, TicketRepository tickets) {
        this.bookingItems = bookingItems;
        this.tickets = tickets;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_TICKET)
    @Transactional
    public void generateTickets(Map<String, Object> payload) {
        Number bookingIdNum = (Number) payload.get("bookingId");
        if (bookingIdNum == null) {
            log.warn("Received ticket generation event without bookingId");
            return;
        }

        Long bookingId = bookingIdNum.longValue();
        log.info("Generating tickets for booking {}", bookingId);

        List<BookingItem> items = bookingItems.findByBookingId(bookingId);
        
        for (BookingItem item : items) {
            if (tickets.findByBookingItemId(item.getId()).isPresent()) {
                log.info("Ticket already generated for booking item {}", item.getId());
                continue;
            }

            Ticket ticket = new Ticket();
            ticket.setBooking(item.getBooking());
            ticket.setBookingItem(item);
            tickets.save(ticket);
            log.info("Generated ticket {} for booking item {}", ticket.getPublicId(), item.getId());
        }
    }
}
