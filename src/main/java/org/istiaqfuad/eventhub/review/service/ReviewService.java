package org.istiaqfuad.eventhub.review.service;

import org.istiaqfuad.eventhub.common.exception.DuplicateResourceException;
import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.review.dto.ReviewRequest;
import org.istiaqfuad.eventhub.review.dto.ReviewResponse;
import org.istiaqfuad.eventhub.review.entity.Review;
import org.istiaqfuad.eventhub.review.repository.ReviewRepository;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Basic review creation/read. One review per user per event is enforced by
 * the DB unique constraint; this pre-checks it for a friendlier error.
 * {@code userId} comes from the authenticated caller.
 */
@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviews;
    private final EventRepository events;
    private final UserRepository users;

    public ReviewService(ReviewRepository reviews, EventRepository events, UserRepository users) {
        this.reviews = reviews;
        this.events = events;
        this.users = users;
    }

    public ReviewResponse create(ReviewRequest request, Long userId) {
        if (reviews.existsByEventIdAndUserId(request.eventId(), userId)) {
            throw new DuplicateResourceException("user has already reviewed event " + request.eventId());
        }
        Review review = new Review();
        review.setEvent(events.getReferenceById(request.eventId()));
        review.setUser(users.getReferenceById(userId));
        review.setRating(request.rating());
        review.setBody(request.body());
        return toResponse(reviews.save(review));
    }

    @Transactional(readOnly = true)
    public ReviewResponse get(Long id) {
        Review review = reviews.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listByEvent(Long eventId) {
        return reviews.findByEventId(eventId).stream().map(this::toResponse).toList();
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getEvent().getId(),
                review.getUser().getId(),
                review.getRating(),
                review.getBody(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
