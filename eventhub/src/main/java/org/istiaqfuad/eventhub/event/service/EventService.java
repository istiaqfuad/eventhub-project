package org.istiaqfuad.eventhub.event.service;

import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.event.dto.EventRequest;
import org.istiaqfuad.eventhub.event.dto.EventResponse;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.entity.EventStatus;
import org.istiaqfuad.eventhub.event.entity.Tag;
import org.istiaqfuad.eventhub.event.repository.CategoryRepository;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.event.repository.TagRepository;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.entity.Organizer;
import org.istiaqfuad.eventhub.user.repository.OrganizerRepository;
import org.istiaqfuad.eventhub.venue.repository.VenueRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashSet;
import java.util.List;
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
    private final org.istiaqfuad.eventhub.event.repository.TicketTypeRepository ticketTypes;

    public EventService(EventRepository events, OrganizerRepository organizers,
                        CategoryRepository categories, VenueRepository venues, TagRepository tags,
                        org.istiaqfuad.eventhub.event.repository.TicketTypeRepository ticketTypes) {
        this.events = events;
        this.organizers = organizers;
        this.categories = categories;
        this.venues = venues;
        this.tags = tags;
        this.ticketTypes = ticketTypes;
    }

    @Transactional(readOnly = true)
    public List<org.istiaqfuad.eventhub.event.dto.TicketTypeResponse> getTicketTypes(Long eventId) {
        return ticketTypes.findByEventId(eventId).stream()
                .map(t -> new org.istiaqfuad.eventhub.event.dto.TicketTypeResponse(
                        t.getId(),
                        t.getEvent().getId(),
                        t.getName(),
                        t.getPrice(),
                        t.getQuota(),
                        t.getCreatedAt(),
                        t.getUpdatedAt()
                )).toList();
    }

    @CacheEvict(value = "events", allEntries = true)
    public EventResponse create(EventRequest request, AuthenticatedUser caller) {
        Event event = new Event();
        event.setOrganizer(resolveOrganizer(request, caller));
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

    /**
     * The owning organizer is the caller's own profile, never trusted from the
     * request body — so a client cannot create events under another organizer.
     * An ADMIN may act on behalf of any organizer by supplying {@code organizerId};
     * when omitted, an admin who is also an organizer falls back to their own profile.
     */
    private Organizer resolveOrganizer(EventRequest request, AuthenticatedUser caller) {
        if (caller.isAdmin() && request.organizerId() != null) {
            return organizers.getReferenceById(request.organizerId());
        }
        return organizers.findByUserId(caller.id())
                .orElseThrow(() -> new AccessDeniedException("Caller is not a registered organizer"));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "events", key = "#id")
    public EventResponse get(Long id) {
        Event event = events.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> list(Pageable pageable) {
        return events.findAll(pageable).map(this::toResponse);
    }

    @CacheEvict(value = "events", key = "#id")
    public EventResponse update(Long id, EventRequest request, AuthenticatedUser caller) {
        Event event = events.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        
        Organizer organizer = resolveOrganizer(request, caller);
        if (!event.getOrganizer().getId().equals(organizer.getId()) && !caller.isAdmin()) {
            throw new AccessDeniedException("You don't have permission to update this event");
        }

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
        if (request.imageUrls() != null) {
            event.getImageUrls().clear();
            event.getImageUrls().addAll(request.imageUrls());
        }
        if (request.tagIds() != null) {
            event.getTags().clear();
            for (Long tagId : request.tagIds()) {
                event.getTags().add(tags.getReferenceById(tagId));
            }
        }
        return toResponse(events.save(event));
    }

    @CacheEvict(value = "events", key = "#id")
    public void delete(Long id, AuthenticatedUser caller) {
        Event event = events.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
                
        // For delete, we use dummy request to resolve organizer or just check directly
        if (!caller.isAdmin()) {
            Organizer organizer = organizers.findByUserId(caller.id())
                    .orElseThrow(() -> new AccessDeniedException("Caller is not a registered organizer"));
            if (!event.getOrganizer().getId().equals(organizer.getId())) {
                throw new AccessDeniedException("You don't have permission to delete this event");
            }
        }
        
        events.delete(event);
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
