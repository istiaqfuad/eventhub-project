package org.istiaqfuad.eventhub.event.repository;

import org.istiaqfuad.eventhub.event.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
