package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.istiaqfuad.eventhub.event.repository.TicketTypeRepository;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Releases the inventory held by {@code PENDING} bookings whose hold has expired:
 * seats HELD→FREE, GA quota returned, booking CANCELLED. Runs on a fixed delay;
 * the per-booking {@link #release} is also the basis for a future cancel endpoint.
 */
@Service
public class BookingExpiryService {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryService.class);
    private static final int BATCH = 200;

    private final BookingRepository bookings;
    private final BookingItemRepository bookingItems;
    private final TicketTypeRepository ticketTypes;

    public BookingExpiryService(BookingRepository bookings, BookingItemRepository bookingItems,
                                TicketTypeRepository ticketTypes) {
        this.bookings = bookings;
        this.bookingItems = bookingItems;
        this.ticketTypes = ticketTypes;
    }

    @Transactional
    public int sweep() {
        List<Booking> expired = bookings.findExpiredPending(OffsetDateTime.now(), PageRequest.of(0, BATCH));
        for (Booking booking : expired) {
            release(booking);
        }
        return expired.size();
    }

    private void release(Booking booking) {
        for (BookingItem item : bookingItems.findByBookingId(booking.getId())) {
            if (item.getSeat() != null) {
                item.getSeat().setStatus(SeatStatus.FREE);
            } else if (item.getTicketType() != null) {
                ticketTypes.release(item.getTicketType().getId(), 1);
            }
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setExpiresAt(null);
    }

    @Scheduled(fixedDelayString = "PT60S")
    public void scheduledSweep() {
        int released = sweep();
        if (released > 0) {
            log.info("Released {} expired booking hold(s)", released);
        }
    }
}
