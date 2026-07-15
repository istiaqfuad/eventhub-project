package org.istiaqfuad.eventhub.common.exception;

import org.istiaqfuad.eventhub.booking.service.InvalidReservationException;
import org.istiaqfuad.eventhub.booking.service.ReservationConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerReservationTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void invalidReservationMapsTo400() {
        ProblemDetail pd = handler.handleInvalidReservation(new InvalidReservationException("bad seat"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getProperties()).containsEntry("code", "INVALID_RESERVATION");
    }

    @Test
    void reservationConflictMapsTo409() {
        ProblemDetail pd = handler.handleReservationConflict(new ReservationConflictException("sold out"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getProperties()).containsEntry("code", "RESERVATION_CONFLICT");
    }

    @Test
    void optimisticLockMapsTo409() {
        ProblemDetail pd = handler.handleOptimisticLock(new OptimisticLockingFailureException("race"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getProperties()).containsEntry("code", "RESERVATION_CONFLICT");
    }
}
