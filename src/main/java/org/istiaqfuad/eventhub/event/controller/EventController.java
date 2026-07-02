package org.istiaqfuad.eventhub.event.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.event.dto.EventRequest;
import org.istiaqfuad.eventhub.event.dto.EventResponse;
import org.istiaqfuad.eventhub.event.entity.EventStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Stub controller for wiring/validation testing only. Returns canned
 * responses with no persistence; replace with service-backed logic later.
 */
@RestController
@RequestMapping(path = "/events", version = "1")
public class EventController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        return new EventResponse(1L, UUID.randomUUID(), request.organizerId(),
                request.title(), request.description(), request.categoryId(),
                request.venueId(), request.city(), request.latitude(), request.longitude(),
                request.startsAt(), request.endsAt(), EventStatus.DRAFT, false,
                request.imageUrls() == null ? Set.of() : request.imageUrls(),
                request.tagIds() == null ? Set.of() : request.tagIds(), now, now);
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        return new EventResponse(id, UUID.randomUUID(), 1L, "Stub Event", "Stub description",
                null, null, "Metropolis", null, null, now, now,
                EventStatus.DRAFT, false, Set.of(), Set.of(), now, now);
    }

    @GetMapping
    public List<EventResponse> list() {
        return List.of();
    }
}
