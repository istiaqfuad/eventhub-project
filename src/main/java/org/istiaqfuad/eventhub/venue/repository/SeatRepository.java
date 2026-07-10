package org.istiaqfuad.eventhub.venue.repository;

import org.istiaqfuad.eventhub.venue.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findBySectionIdIn(List<Long> sectionIds);
}
