package org.istiaqfuad.eventhub.venue.repository;

import org.istiaqfuad.eventhub.venue.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByVenueId(Long venueId);
}
