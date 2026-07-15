package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Cancels {@code PENDING} bookings whose hold has expired and releases their inventory via
 * {@link BookingInventoryService}. Runs on a fixed delay.
 */
@Service
public class BookingExpiryService {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryService.class);
    private static final int BATCH = 200;

    private final BookingRepository bookings;
    private final BookingInventoryService inventory;

    public BookingExpiryService(BookingRepository bookings, BookingInventoryService inventory) {
        this.bookings = bookings;
        this.inventory = inventory;
    }

    /** Releases every expired hold in one transaction. */
    @Transactional
    public int sweep() {
        List<Booking> expired = bookings.findExpiredPending(OffsetDateTime.now(), PageRequest.of(0, BATCH));
        for (Booking booking : expired) {
            inventory.release(booking);
        }
        return expired.size();
    }

    // @Transactional here (not only on sweep) so the scheduler's proxied call opens a
    // transaction that spans the lazy loads and dirty-checked writes inside the sweep;
    // a self-call to sweep() alone would run without one and fail on lazy access.
    @Scheduled(fixedDelayString = "PT60S")
    @Transactional
    public void scheduledSweep() {
        int released = sweep();
        if (released > 0) {
            log.info("Released {} expired booking hold(s)", released);
        }
    }
}
