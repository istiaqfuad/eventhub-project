package org.istiaqfuad.eventhub.outbox.service;

import org.istiaqfuad.eventhub.outbox.entity.OutboxEvent;
import org.istiaqfuad.eventhub.outbox.entity.OutboxStatus;
import org.istiaqfuad.eventhub.outbox.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Polls {@code outbox_events} for PENDING rows and publishes them to RabbitMQ,
 * then marks each row PROCESSED (or FAILED on publish error).
 *
 * <p>Runs every 500 ms. Each cycle processes at most 100 events in one transaction
 * so the DB lock window is bounded. Because publish happens inside the transaction,
 * a crash between ZADD and PROCESSED will leave the row PENDING for the next cycle
 * (at-least-once delivery — consumers must be idempotent on event id).
 */
@Service
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbit;

    public OutboxRelay(OutboxEventRepository repository, RabbitTemplate rabbit) {
        this.repository = repository;
        this.rabbit = rabbit;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void relay() {
        List<OutboxEvent> pending =
                repository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pending) {
            try {
                rabbit.convertAndSend(
                        "eventhub.exchange",
                        event.getEventType(),
                        event.getPayload());
                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(OffsetDateTime.now());
            } catch (Exception ex) {
                log.error("Failed to relay outbox event {} ({}): {}",
                        event.getId(), event.getEventType(), ex.getMessage());
                event.setStatus(OutboxStatus.FAILED);
            }
        }
    }
}
