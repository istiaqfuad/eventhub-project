package org.istiaqfuad.eventhub.venue.repository;

import org.istiaqfuad.eventhub.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
