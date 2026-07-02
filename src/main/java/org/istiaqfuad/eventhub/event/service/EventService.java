package org.istiaqfuad.eventhub.event.service;

import org.istiaqfuad.eventhub.event.dto.EventRequest;
import org.istiaqfuad.eventhub.event.dto.EventResponse;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.entity.EventStatus;
import org.istiaqfuad.eventhub.event.entity.Tag;
import org.istiaqfuad.eventhub.event.repository.CategoryRepository;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.event.repository.TagRepository;
import org.istiaqfuad.eventhub.user.repository.OrganizerRepository;
import org.istiaqfuad.eventhub.venue.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Basic event creation/read. Foreign keys are attached as lazy references
 * ({@code getReferenceById}) so no extra selects are issued; the create is
 * always DRAFT and not high-demand (both server-owned).
 */
@Service
@Transactional
public class EventService {

    private final EventRepository events;
    private final OrganizerRepository organizers;
    private final CategoryRepository categories;
    private final VenueRepository venues;
    private final TagRepository tags;

    public EventService(EventRepository events, OrganizerRepository organizers,
                        CategoryRepository categories, VenueRepository venues, TagRepository tags) {
        this.events = events;
        this.organizers = organizers;
        this.categories = categories;
        this.venues = venues;
        this.tags = tags;
    }

    public EventResponse create(EventRequest request) {
        Event event = new Event();
        event.setOrganizer(organizers.getReferenceById(request.organizerId()));
        event.setTitle(request.title());
        event.setDescription(request.description());
        if (request.categoryId() != null) {
            event.setCategory(categories.getReferenceById(request.categoryId()));
        }
        if (request.venueId() != null) {
            event.setVenue(venues.getReferenceById(request.venueId()));
        }
        event.setCity(request.city());
        event.setLatitude(request.latitude());
        event.setLongitude(request.longitude());
        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());
        event.setStatus(EventStatus.DRAFT);
        event.setHighDemand(false);
        if (request.imageUrls() != null) {
            event.getImageUrls().addAll(request.imageUrls());
        }
        if (request.tagIds() != null) {
            for (Long tagId : request.tagIds()) {
                event.getTags().add(tags.getReferenceById(tagId));
            }
        }
        return toResponse(events.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long id) {
        Event event = events.findById(id)
                .orElseThrow(() -> new NoSuchElementException("event not found: " + id));
        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list() {
        return events.findAll().stream().map(this::toResponse).toList();
    }

    private EventResponse toResponse(Event event) {
        Set<Long> tagIds = event.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new EventResponse(
                event.getId(),
                event.getPublicId(),
                event.getOrganizer().getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory() == null ? null : event.getCategory().getId(),
                event.getVenue() == null ? null : event.getVenue().getId(),
                event.getCity(),
                event.getLatitude(),
                event.getLongitude(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getStatus(),
                Boolean.TRUE.equals(event.getHighDemand()),
                new LinkedHashSet<>(event.getImageUrls()),
                tagIds,
                event.getCreatedAt(),
                event.getUpdatedAt());
    }
}
