package org.istiaqfuad.eventhub.event.search;

import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EventSearchSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventSearchSyncConsumer.class);

    private final EventRepository eventRepository;
    private final EventSearchRepository searchRepository;

    public EventSearchSyncConsumer(EventRepository eventRepository, EventSearchRepository searchRepository) {
        this.eventRepository = eventRepository;
        this.searchRepository = searchRepository;
    }

    @RabbitListener(queues = "event.index")
    public void onEventSync(Map<String, Object> payload) {
        String eventType = (String) payload.get("eventType");
        String idStr = String.valueOf(payload.get("eventId"));
        Long eventId = Long.valueOf(idStr);

        log.info("Received {} for event {}", eventType, eventId);

        if ("EventDeleted".equals(eventType)) {
            searchRepository.deleteById(idStr);
            log.info("Removed event {} from Elasticsearch", idStr);
            return;
        }

        // For created and updated, fetch latest state from PostgreSQL
        eventRepository.findById(eventId).ifPresentOrElse(event -> {
            EventDocument doc = mapToDocument(event);
            searchRepository.save(doc);
            log.info("Indexed event {} in Elasticsearch", idStr);
        }, () -> {
            log.warn("Event {} not found in PostgreSQL; skipping indexing", eventId);
        });
    }

    private EventDocument mapToDocument(Event event) {
        GeoPoint location = null;
        if (event.getLatitude() != null && event.getLongitude() != null) {
            location = new GeoPoint(event.getLatitude(), event.getLongitude());
        }

        return EventDocument.builder()
                .id(event.getId().toString())
                .publicId(event.getPublicId() != null ? event.getPublicId().toString() : null)
                .title(event.getTitle())
                .description(event.getDescription())
                .categoryName(event.getCategory() != null ? event.getCategory().getName() : null)
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .tags(event.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()))
                .city(event.getCity())
                .location(location)
                .startsAt(event.getStartsAt())
                .endsAt(event.getEndsAt())
                .highDemand(event.getHighDemand())
                .build();
    }
}
