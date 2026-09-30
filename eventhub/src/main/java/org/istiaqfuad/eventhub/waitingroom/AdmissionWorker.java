package org.istiaqfuad.eventhub.waitingroom;

import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Scheduled worker that drains the waiting queue for high-demand events
 * and issues admission tokens.
 *
 * <p>Every poll interval, {@code ZPOPMIN waitroom:{eventId} drainPerSecond} atomically
 * removes the top-ranked users and sets admission tokens with a TTL. Only
 * users with a valid token can proceed to book. Unused tokens expire naturally
 * so capacity slots are reclaimed.
 *
 * <p><b>Neon compute gate:</b> the worker checks {@code SCARD waitroom:active} first
 * and returns immediately when no queue exists, so the per-second Postgres query
 * only runs when users are actually waiting. This is what lets the database reach
 * Neon's 5-minute idle threshold and scale to zero on the free tier.
 */
@Service
@EnableConfigurationProperties(WaitingRoomProperties.class)
public class AdmissionWorker {

    private static final Logger log = LoggerFactory.getLogger(AdmissionWorker.class);

    private final StringRedisTemplate redis;
    private final EventRepository events;
    private final WaitingRoomProperties properties;
    private final WaitingRoomService waitingRoom;

    public AdmissionWorker(StringRedisTemplate redis,
                           EventRepository events,
                           WaitingRoomProperties properties,
                           WaitingRoomService waitingRoom) {
        this.redis = redis;
        this.events = events;
        this.properties = properties;
        this.waitingRoom = waitingRoom;
    }

    @Scheduled(fixedDelayString = "${app.waiting-room.poll-delay:1000}")
    public void admit() {
        if (waitingRoom.activeQueueCount() == 0) {
            return;
        }
        List<Long> highDemandIds = findHighDemandEventIds();
        for (Long eventId : highDemandIds) {
            String queueKey = "waitroom:" + eventId;
            Set<ZSetOperations.TypedTuple<String>> batch =
                    redis.opsForZSet().popMin(queueKey, properties.drainPerSecond());
            if (batch == null || batch.isEmpty()) {
                waitingRoom.markQueueInactive(eventId);
                continue;
            }
            for (ZSetOperations.TypedTuple<String> entry : batch) {
                String userId = entry.getValue();
                String tokenKey = "admit:" + eventId + ":" + userId;
                redis.opsForValue().set(tokenKey, "1", properties.admissionTtl());
                log.debug("Admitted userId={} for eventId={}", userId, eventId);
            }
        }
    }

    private List<Long> findHighDemandEventIds() {
        return events.findByHighDemandTrue().stream()
                .map(Event::getId)
                .toList();
    }
}
