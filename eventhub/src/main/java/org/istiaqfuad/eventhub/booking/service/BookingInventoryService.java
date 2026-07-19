package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.event.repository.TicketTypeRepository;
import org.istiaqfuad.eventhub.outbox.service.OutboxService;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The inventory transitions a booking's hold can take, shared by the expiry sweeper
 * (release) and the payment flow (confirm on success, release on failure/expiry).
 * Both mutate managed entities and rely on the caller's transaction.
 */
@Service
public class BookingInventoryService {

    private final BookingItemRepository bookingItems;
    private final TicketTypeRepository ticketTypes;
    private final OutboxService outbox;

    public BookingInventoryService(BookingItemRepository bookingItems,
                                   TicketTypeRepository ticketTypes,
                                   OutboxService outbox) {
        this.bookingItems = bookingItems;
        this.ticketTypes = ticketTypes;
        this.outbox = outbox;
    }

    /**
     * Commits the hold: held seats become BOOKED, the booking CONFIRMED.
     * GA quota is already counted and does not change here.
     *
     * <p>Retried up to 3 times with exponential backoff on
     * {@link ObjectOptimisticLockingFailureException} — the Seat entity carries
     * a {@code @Version} column; two concurrent confirms of the same seat will
     * cause one to lose and retry here rather than surfacing a raw error.
     */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    public void confirm(Booking booking) {
        for (BookingItem item : bookingItems.findByBookingId(booking.getId())) {
            if (item.getSeat() != null) {
                item.getSeat().setStatus(SeatStatus.BOOKED);
            }
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        outbox.record(
                "Booking",
                booking.getId().toString(),
                "BookingConfirmed",
                java.util.Map.of(
                        "bookingId", booking.getId(),
                        "userId",    booking.getUser().getId(),
                        "eventId",   booking.getEvent().getId(),
                        "total",     booking.getTotal().toPlainString()
                )
        );
    }

    /**
     * Recovery method invoked after all retries on {@link ObjectOptimisticLockingFailureException}
     * are exhausted. Surfaces a clean domain exception instead of leaking ORM internals.
     */
    @Recover
    public void recoverConfirm(ObjectOptimisticLockingFailureException ex, Booking booking) {
        throw new ReservationConflictException(
                "Seat was taken by another booking — could not confirm booking " + booking.getId());
    }

    /**
     * Releases the hold: held seats return to FREE, GA quota is returned, the booking CANCELLED.
     * The line items are deleted — otherwise the {@code uq_booking_item_seat} unique constraint
     * (which spans all bookings) would keep a released seat permanently unbookable.
     */
    public void release(Booking booking) {
        List<BookingItem> items = bookingItems.findByBookingId(booking.getId());
        for (BookingItem item : items) {
            if (item.getSeat() != null) {
                item.getSeat().setStatus(SeatStatus.FREE);
            } else if (item.getTicketType() != null) {
                ticketTypes.release(item.getTicketType().getId(), 1);
            }
        }
        bookingItems.deleteAll(items);
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setExpiresAt(null);
    }
}
