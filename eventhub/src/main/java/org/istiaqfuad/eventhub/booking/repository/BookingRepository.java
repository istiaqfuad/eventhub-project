package org.istiaqfuad.eventhub.booking.repository;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    @Query("select b from Booking b where b.status = org.istiaqfuad.eventhub.booking.entity.BookingStatus.PENDING and b.expiresAt < :now")
    List<Booking> findExpiredPending(@Param("now") OffsetDateTime now, Pageable pageable);
}
