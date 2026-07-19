package org.istiaqfuad.eventhub.waitingroom;

import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Manages the virtual waiting room for high-demand events.
 *
 * <p>Redis keys:
 * <ul>
 *   <li>{@code waitroom:{eventId}:seq} — monotonic counter for FIFO ordering</li>
 *   <li>{@code waitroom:{eventId}} — sorted set: member=userId, score=seq</li>
 *   <li>{@code admit:{eventId}:{userId}} — admission token (exists = admitted)</li>
 * </ul>
 *
 * <p>All operations are O(log N) on the sorted set.
 */
@Service
public class WaitingRoomService {

    private final StringRedisTemplate redis;
    private final EventRepository events;

    public WaitingRoomService(StringRedisTemplate redis, EventRepository events) {
        this.redis = redis;
        this.events = events;
    }

    /**
     * Joins the queue for a high-demand event.
     *
     * @return 0-indexed queue position (0 = next to be admitted)
     * @throws IllegalStateException if the event is not marked high-demand
     */
    public long join(long eventId, long userId) {
        assertHighDemand(eventId);
        String seqKey   = "waitroom:" + eventId + ":seq";
        String queueKey = "waitroom:" + eventId;

        // INCR is atomic — monotonic sequence beats clock collisions
        long seq = redis.opsForValue().increment(seqKey);
        redis.opsForZSet().add(queueKey, String.valueOf(userId), seq);

        Long rank = redis.opsForZSet().rank(queueKey, String.valueOf(userId));
        return rank != null ? rank : 0;
    }

    /**
     * Returns the user's current waiting-room status.
     * Admitted if the admit token exists; queued with position otherwise.
     */
    public WaitingRoomStatus getStatus(long eventId, long userId) {
        if (hasAdmissionToken(eventId, userId)) {
            return WaitingRoomStatus.admitted();
        }
        Long rank = redis.opsForZSet().rank(
                "waitroom:" + eventId, String.valueOf(userId));
        if (rank == null) {
            return WaitingRoomStatus.notInQueue();
        }
        return WaitingRoomStatus.queued(rank);
    }

    /**
     * Returns {@code true} if the user holds a valid (non-expired) admission token.
     * Used by {@link org.istiaqfuad.eventhub.booking.service.BookingService}
     * to gate access to the booking flow.
     */
    public boolean hasAdmissionToken(long eventId, long userId) {
        return Boolean.TRUE.equals(
                redis.hasKey("admit:" + eventId + ":" + userId));
    }

    private void assertHighDemand(long eventId) {
        Event event = events.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        if (!Boolean.TRUE.equals(event.getHighDemand())) {
            throw new IllegalStateException(
                    "Event " + eventId + " does not have a waiting room");
        }
    }
}
