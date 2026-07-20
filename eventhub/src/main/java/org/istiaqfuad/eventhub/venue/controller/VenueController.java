package org.istiaqfuad.eventhub.venue.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.venue.dto.SeatRequest;
import org.istiaqfuad.eventhub.venue.dto.SeatResponse;
import org.istiaqfuad.eventhub.venue.dto.SectionRequest;
import org.istiaqfuad.eventhub.venue.dto.SectionResponse;
import org.istiaqfuad.eventhub.venue.dto.VenueRequest;
import org.istiaqfuad.eventhub.venue.dto.VenueResponse;
import org.istiaqfuad.eventhub.venue.service.VenueService;
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
@RequestMapping(path = "/venues", version = "1")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public VenueResponse create(@Valid @RequestBody VenueRequest request) {
        return venueService.create(request);
    }

    @GetMapping("/{id}")
    public VenueResponse get(@PathVariable Long id) {
        return venueService.get(id);
    }

    @GetMapping("/{id}/layout")
    public org.istiaqfuad.eventhub.venue.dto.VenueLayoutResponse getLayout(@PathVariable Long id) {
        return venueService.getLayout(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public VenueResponse update(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        return venueService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }

    @GetMapping
    public Page<VenueResponse> list(Pageable pageable) {
        return venueService.list(pageable);
    }

    // --- Sections ---

    @PostMapping("/{id}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public SectionResponse createSection(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        // Enforce venueId in request matches path
        if (!id.equals(request.venueId())) {
            throw new IllegalArgumentException("Venue ID in path does not match request body");
        }
        return venueService.createSection(request);
    }

    @PutMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public SectionResponse updateSection(@PathVariable Long sectionId, @Valid @RequestBody SectionRequest request) {
        return venueService.updateSection(sectionId, request);
    }

    @DeleteMapping("/sections/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public void deleteSection(@PathVariable Long sectionId) {
        venueService.deleteSection(sectionId);
    }

    // --- Seats ---

    @PostMapping("/sections/{sectionId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public SeatResponse createSeat(@PathVariable Long sectionId, @Valid @RequestBody SeatRequest request) {
        if (!sectionId.equals(request.sectionId())) {
            throw new IllegalArgumentException("Section ID in path does not match request body");
        }
        return venueService.createSeat(request);
    }

    @PutMapping("/seats/{seatId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public SeatResponse updateSeat(@PathVariable Long seatId, @Valid @RequestBody SeatRequest request) {
        return venueService.updateSeat(seatId, request);
    }

    @DeleteMapping("/seats/{seatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public void deleteSeat(@PathVariable Long seatId) {
        venueService.deleteSeat(seatId);
    }
}
