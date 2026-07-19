package org.istiaqfuad.eventhub.outbox.repository;

import org.istiaqfuad.eventhub.outbox.entity.OutboxEvent;
import org.istiaqfuad.eventhub.outbox.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Fetches the next batch of unprocessed events in insertion order.
     * Limited to 100 to bound each relay cycle's transaction duration.
     */
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
