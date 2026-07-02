package org.istiaqfuad.eventhub.event.repository;

import org.istiaqfuad.eventhub.event.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
