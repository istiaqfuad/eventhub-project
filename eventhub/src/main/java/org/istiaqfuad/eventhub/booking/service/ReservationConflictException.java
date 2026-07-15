package org.istiaqfuad.eventhub.booking.service;

/** Requested inventory is unavailable (seat taken, GA sold out). Maps to 409. */
public class ReservationConflictException extends RuntimeException {
    public ReservationConflictException(String message) {
        super(message);
    }
}
