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
 * <p>Every second, {@code ZPOPMIN waitroom:{eventId} drainPerSecond} atomically
 * removes the top-ranked users and sets admission tokens with a TTL. Only
 * users with a valid token can proceed to book. Unused tokens expire naturally
 * so capacity slots are reclaimed.
 */
@Service
@EnableConfigurationProperties(WaitingRoomProperties.class)
public class AdmissionWorker {

    private static final Logger log = LoggerFactory.getLogger(AdmissionWorker.class);

    private final StringRedisTemplate redis;
    private final EventRepository events;
    private final WaitingRoomProperties properties;

    public AdmissionWorker(StringRedisTemplate redis,
                           EventRepository events,
                           WaitingRoomProperties properties) {
        this.redis = redis;
        this.events = events;
        this.properties = properties;
    }

    @Scheduled(fixedDelay = 1000)
    public void admit() {
        List<Long> highDemandIds = findHighDemandEventIds();
        for (Long eventId : highDemandIds) {
            String queueKey = "waitroom:" + eventId;
            Set<ZSetOperations.TypedTuple<String>> batch =
                    redis.opsForZSet().popMin(queueKey, properties.drainPerSecond());
            if (batch == null || batch.isEmpty()) continue;

            for (ZSetOperations.TypedTuple<String> entry : batch) {
                String userId = entry.getValue();
                String tokenKey = "admit:" + eventId + ":" + userId;
                redis.opsForValue().set(tokenKey, "1", properties.admissionTtl());
                log.debug("Admitted userId={} for eventId={}", userId, eventId);
            }
        }
    }

    private List<Long> findHighDemandEventIds() {
        return events.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getHighDemand()))
                .map(Event::getId)
                .toList();
    }
}
