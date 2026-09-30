package org.istiaqfuad.eventhub.outbox.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Drives {@link OutboxRelay} with an adaptive poll interval: 500 ms while the
 * outbox is draining, 5 s while it is idle, so an idle app stops issuing
 * 2 SELECTs per second against the database.
 */
@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);
    private static final long BUSY_DELAY_MS = 500;
    private static final long IDLE_DELAY_MS = 30000;

    private final TaskScheduler taskScheduler;
    private final OutboxRelay relay;

    public OutboxRelayScheduler(TaskScheduler taskScheduler, OutboxRelay relay) {
        this.taskScheduler = taskScheduler;
        this.relay = relay;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        scheduleNext(0);
    }

    private void scheduleNext(long delayMs) {
        taskScheduler.schedule(this::runCycle, Instant.now().plusMillis(delayMs));
    }

    private void runCycle() {
        long nextDelay = IDLE_DELAY_MS;
        try {
            if (relay.relay() > 0) {
                nextDelay = BUSY_DELAY_MS;
            }
        } catch (Exception ex) {
            log.error("Outbox relay cycle failed: {}", ex.getMessage());
        }
        scheduleNext(nextDelay);
    }
}
