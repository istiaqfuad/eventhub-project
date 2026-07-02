package org.istiaqfuad.eventhub.booking.repository;

import org.istiaqfuad.eventhub.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
}
