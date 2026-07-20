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

import org.istiaqfuad.eventhub.venue.dto.SeatRequest;
import org.istiaqfuad.eventhub.venue.dto.SeatResponse;
import org.istiaqfuad.eventhub.venue.dto.SectionRequest;
import org.istiaqfuad.eventhub.venue.dto.SectionResponse;
import org.istiaqfuad.eventhub.venue.entity.Seat;
import org.istiaqfuad.eventhub.venue.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    public Page<VenueResponse> list(Pageable pageable) {
        return venues.findAll(pageable).map(this::toResponse);
    }

    @CacheEvict(value = "venues", key = "#id")
    public VenueResponse update(Long id, VenueRequest request) {
        Venue venue = venues.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
        venue.setName(request.name());
        venue.setLayoutType(request.layoutType());
        venue.setAddress(request.address());
        venue.setCity(request.city());
        return toResponse(venues.save(venue));
    }

    @CacheEvict(value = "venues", key = "#id")
    public void delete(Long id) {
        Venue venue = venues.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
        venues.delete(venue);
    }

    @CacheEvict(value = "venues", allEntries = true)
    public SectionResponse createSection(SectionRequest request) {
        Venue venue = venues.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", request.venueId()));
        Section section = new Section();
        section.setVenue(venue);
        section.setName(request.name());
        section.setSeatType(request.seatType());
        section.setBasePrice(request.basePrice());
        section = sections.save(section);
        return new SectionResponse(section.getId(), venue.getId(), section.getName(), section.getSeatType(), section.getBasePrice(), section.getCreatedAt(), section.getUpdatedAt());
    }

    @CacheEvict(value = "venues", allEntries = true)
    public SectionResponse updateSection(Long id, SectionRequest request) {
        Section section = sections.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", id));
        section.setName(request.name());
        section.setSeatType(request.seatType());
        section.setBasePrice(request.basePrice());
        section = sections.save(section);
        return new SectionResponse(section.getId(), section.getVenue().getId(), section.getName(), section.getSeatType(), section.getBasePrice(), section.getCreatedAt(), section.getUpdatedAt());
    }

    @CacheEvict(value = "venues", allEntries = true)
    public void deleteSection(Long id) {
        Section section = sections.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", id));
        sections.delete(section);
    }

    @CacheEvict(value = "venues", allEntries = true)
    public SeatResponse createSeat(SeatRequest request) {
        Section section = sections.findById(request.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", request.sectionId()));
        Seat seat = new Seat();
        seat.setSection(section);
        seat.setRowLabel(request.rowLabel());
        seat.setColNumber(request.colNumber());
        seat.setStatus(org.istiaqfuad.eventhub.venue.entity.SeatStatus.FREE);
        seat = seats.save(seat);
        return new SeatResponse(seat.getId(), section.getId(), seat.getRowLabel(), seat.getColNumber(), seat.getStatus(), seat.getVersion(), seat.getCreatedAt(), seat.getUpdatedAt());
    }

    @CacheEvict(value = "venues", allEntries = true)
    public SeatResponse updateSeat(Long id, SeatRequest request) {
        Seat seat = seats.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", id));
        seat.setRowLabel(request.rowLabel());
        seat.setColNumber(request.colNumber());
        seat = seats.save(seat);
        return new SeatResponse(seat.getId(), seat.getSection().getId(), seat.getRowLabel(), seat.getColNumber(), seat.getStatus(), seat.getVersion(), seat.getCreatedAt(), seat.getUpdatedAt());
    }

    @CacheEvict(value = "venues", allEntries = true)
    public void deleteSeat(Long id) {
        Seat seat = seats.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", id));
        seats.delete(seat);
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
