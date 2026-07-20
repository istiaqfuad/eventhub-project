package org.istiaqfuad.eventhub.event.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.event.dto.EventRequest;
import org.istiaqfuad.eventhub.event.dto.EventResponse;
import org.istiaqfuad.eventhub.event.service.EventService;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.security.web.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/events", version = "1")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public EventResponse create(@Valid @RequestBody EventRequest request,
                                @CurrentUser AuthenticatedUser caller) {
        return eventService.create(request, caller);
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) {
        return eventService.get(id);
    }

    @GetMapping("/{id}/tickets")
    public List<org.istiaqfuad.eventhub.event.dto.TicketTypeResponse> getTicketTypes(@PathVariable Long id) {
        return eventService.getTicketTypes(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public EventResponse update(@PathVariable Long id, 
                                @Valid @RequestBody EventRequest request,
                                @CurrentUser AuthenticatedUser caller) {
        return eventService.update(id, request, caller);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public void delete(@PathVariable Long id, @CurrentUser AuthenticatedUser caller) {
        eventService.delete(id, caller);
    }

    @GetMapping
    public Page<EventResponse> list(Pageable pageable) {
        return eventService.list(pageable);
    }
}
