package org.istiaqfuad.eventhub.event.repository;

import org.istiaqfuad.eventhub.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByHighDemandTrue();
}
