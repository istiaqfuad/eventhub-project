package org.istiaqfuad.eventhub.analytics.repository;

import org.istiaqfuad.eventhub.analytics.entity.EventStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventStatRepository extends JpaRepository<EventStat, Long> {

    Optional<EventStat> findByEventId(Long eventId);
}
