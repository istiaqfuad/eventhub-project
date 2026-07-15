package org.istiaqfuad.eventhub.event.repository;

import org.istiaqfuad.eventhub.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
