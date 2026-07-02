package org.istiaqfuad.eventhub.venue.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.venue.dto.VenueRequest;
import org.istiaqfuad.eventhub.venue.dto.VenueResponse;
import org.istiaqfuad.eventhub.venue.entity.LayoutType;
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

/**
 * Stub controller for wiring/validation testing only. Returns canned
 * responses with no persistence; replace with service-backed logic later.
 */
@RestController
@RequestMapping(path = "/venues", version = "1")
public class VenueController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VenueResponse create(@Valid @RequestBody VenueRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        return new VenueResponse(1L, request.name(), request.layoutType(),
                request.address(), request.city(), now, now);
    }

    @GetMapping("/{id}")
    public VenueResponse get(@PathVariable Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        return new VenueResponse(id, "Stub Arena", LayoutType.STADIUM,
                "123 Example St", "Metropolis", now, now);
    }

    @GetMapping
    public List<VenueResponse> list() {
        return List.of();
    }
}
