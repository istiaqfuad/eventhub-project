package org.istiaqfuad.eventhub.booking.service;

/** A booking request is structurally invalid (wrong venue, wrong event, duplicate seat). Maps to 400. */
public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException(String message) {
        super(message);
    }
}
