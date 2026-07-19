package org.istiaqfuad.eventhub.outbox.service;

import org.istiaqfuad.eventhub.outbox.entity.OutboxEvent;
import org.istiaqfuad.eventhub.outbox.entity.OutboxStatus;
import org.istiaqfuad.eventhub.outbox.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Records an outbox event as part of the caller's existing transaction
 * (propagation = MANDATORY enforces this contract — calling without an active
 * transaction throws immediately rather than silently skipping the write).
 *
 * <p>The event is written to {@code outbox_events} atomically with whatever
 * domain state the caller is committing. The relay then polls and publishes
 * without any cross-service 2PC.
 */
@Service
public class OutboxService {

    private final OutboxEventRepository repository;

    public OutboxService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String aggregateType, String aggregateId,
                       String eventType, Map<String, Object> payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setStatus(OutboxStatus.PENDING);
        event.setCreatedAt(OffsetDateTime.now());
        repository.save(event);
    }
}
