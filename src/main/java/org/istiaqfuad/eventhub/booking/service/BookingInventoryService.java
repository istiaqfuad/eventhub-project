package org.istiaqfuad.eventhub.booking.service;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.istiaqfuad.eventhub.booking.entity.BookingItem;
import org.istiaqfuad.eventhub.booking.entity.BookingStatus;
import org.istiaqfuad.eventhub.booking.repository.BookingItemRepository;
import org.istiaqfuad.eventhub.event.repository.TicketTypeRepository;
import org.istiaqfuad.eventhub.venue.entity.SeatStatus;
import org.springframework.stereotype.Service;

/**
 * The inventory transitions a booking's hold can take, shared by the expiry sweeper
 * (release) and the payment flow (confirm on success, release on failure/expiry).
 * Both mutate managed entities and rely on the caller's transaction.
 */
@Service
public class BookingInventoryService {

    private final BookingItemRepository bookingItems;
    private final TicketTypeRepository ticketTypes;

    public BookingInventoryService(BookingItemRepository bookingItems, TicketTypeRepository ticketTypes) {
        this.bookingItems = bookingItems;
        this.ticketTypes = ticketTypes;
    }

    /** Commits the hold: held seats become BOOKED, the booking CONFIRMED. GA quota is already counted. */
    public void confirm(Booking booking) {
        for (BookingItem item : bookingItems.findByBookingId(booking.getId())) {
            if (item.getSeat() != null) {
                item.getSeat().setStatus(SeatStatus.BOOKED);
            }
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
    }

    /** Releases the hold: held seats return to FREE, GA quota is returned, the booking CANCELLED. */
    public void release(Booking booking) {
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
}
