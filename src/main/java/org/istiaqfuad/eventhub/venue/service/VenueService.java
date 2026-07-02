package org.istiaqfuad.eventhub.venue.service;

import org.istiaqfuad.eventhub.venue.dto.VenueRequest;
import org.istiaqfuad.eventhub.venue.dto.VenueResponse;
import org.istiaqfuad.eventhub.venue.entity.Venue;
import org.istiaqfuad.eventhub.venue.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Basic venue CRUD. A venue is self-contained (no foreign keys), so this is
 * a straight DTO ↔ entity mapping.
 */
@Service
@Transactional
public class VenueService {

    private final VenueRepository venues;

    public VenueService(VenueRepository venues) {
        this.venues = venues;
    }

    public VenueResponse create(VenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name());
        venue.setLayoutType(request.layoutType());
        venue.setAddress(request.address());
        venue.setCity(request.city());
        return toResponse(venues.save(venue));
    }

    @Transactional(readOnly = true)
    public VenueResponse get(Long id) {
        Venue venue = venues.findById(id)
                .orElseThrow(() -> new NoSuchElementException("venue not found: " + id));
        return toResponse(venue);
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> list() {
        return venues.findAll().stream().map(this::toResponse).toList();
    }

    private VenueResponse toResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getLayoutType(),
                venue.getAddress(),
                venue.getCity(),
                venue.getCreatedAt(),
                venue.getUpdatedAt());
    }
}
