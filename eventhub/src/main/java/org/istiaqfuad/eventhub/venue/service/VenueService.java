package org.istiaqfuad.eventhub.venue.service;

import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.venue.dto.VenueRequest;
import org.istiaqfuad.eventhub.venue.dto.VenueResponse;
import org.istiaqfuad.eventhub.venue.entity.Venue;
import org.istiaqfuad.eventhub.venue.repository.VenueRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Basic venue CRUD. A venue is self-contained (no foreign keys), so this is
 * a straight DTO ↔ entity mapping.
 */
@Service
@Transactional
public class VenueService {

    private final VenueRepository venues;
    private final org.istiaqfuad.eventhub.venue.repository.SectionRepository sections;
    private final org.istiaqfuad.eventhub.venue.repository.SeatRepository seats;

    public VenueService(VenueRepository venues,
                        org.istiaqfuad.eventhub.venue.repository.SectionRepository sections,
                        org.istiaqfuad.eventhub.venue.repository.SeatRepository seats) {
        this.venues = venues;
        this.sections = sections;
        this.seats = seats;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "venues", key = "#id + ':layout'")
    public org.istiaqfuad.eventhub.venue.dto.VenueLayoutResponse getLayout(Long id) {
        VenueResponse venue = get(id);
        List<org.istiaqfuad.eventhub.venue.entity.Section> venueSections = sections.findByVenueId(id);
        
        List<Long> sectionIds = venueSections.stream().map(org.istiaqfuad.eventhub.venue.entity.Section::getId).toList();
        List<org.istiaqfuad.eventhub.venue.entity.Seat> allSeats = sectionIds.isEmpty() ? List.of() : seats.findBySectionIdIn(sectionIds);
        
        List<org.istiaqfuad.eventhub.venue.dto.VenueLayoutResponse.SectionWithSeatsResponse> sectionResponses = venueSections.stream().map(sec -> {
            org.istiaqfuad.eventhub.venue.dto.SectionResponse secResp = new org.istiaqfuad.eventhub.venue.dto.SectionResponse(
                    sec.getId(), sec.getVenue().getId(), sec.getName(), sec.getSeatType(), sec.getBasePrice(), sec.getCreatedAt(), sec.getUpdatedAt()
            );
            List<org.istiaqfuad.eventhub.venue.dto.SeatResponse> secSeats = allSeats.stream()
                    .filter(s -> s.getSection().getId().equals(sec.getId()))
                    .map(s -> new org.istiaqfuad.eventhub.venue.dto.SeatResponse(
                            s.getId(), s.getSection().getId(), s.getRowLabel(), s.getColNumber(), s.getStatus(), s.getVersion(), s.getCreatedAt(), s.getUpdatedAt()
                    )).toList();
            return new org.istiaqfuad.eventhub.venue.dto.VenueLayoutResponse.SectionWithSeatsResponse(secResp, secSeats);
        }).toList();

        return new org.istiaqfuad.eventhub.venue.dto.VenueLayoutResponse(venue, sectionResponses);
    }

    @Caching(evict = {
            @CacheEvict(value = "venues", allEntries = true)
    })
    public VenueResponse create(VenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name());
        venue.setLayoutType(request.layoutType());
        venue.setAddress(request.address());
        venue.setCity(request.city());
        return toResponse(venues.save(venue));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "venues", key = "#id")
    public VenueResponse get(Long id) {
        Venue venue = venues.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
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
