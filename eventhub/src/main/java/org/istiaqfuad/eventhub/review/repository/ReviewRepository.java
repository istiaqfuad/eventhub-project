package org.istiaqfuad.eventhub.review.repository;

import org.istiaqfuad.eventhub.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByEventId(Long eventId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
