package org.istiaqfuad.eventhub.event.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSearchRepository extends ElasticsearchRepository<EventDocument, String> {
    Page<EventDocument> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);
    
    Page<EventDocument> findByCategoryName(String categoryName, Pageable pageable);
    
    Page<EventDocument> findByCity(String city, Pageable pageable);
}
