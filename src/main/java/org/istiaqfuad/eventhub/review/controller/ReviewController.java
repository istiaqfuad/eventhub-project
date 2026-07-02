package org.istiaqfuad.eventhub.review.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.review.dto.ReviewRequest;
import org.istiaqfuad.eventhub.review.dto.ReviewResponse;
import org.istiaqfuad.eventhub.review.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/reviews", version = "1")
public class ReviewController {

    // TODO: replace with the authenticated principal once Spring Security is wired.
    private static final Long PLACEHOLDER_USER_ID = 1L;

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewRequest request) {
        return reviewService.create(request, PLACEHOLDER_USER_ID);
    }

    @GetMapping("/{id}")
    public ReviewResponse get(@PathVariable Long id) {
        return reviewService.get(id);
    }
}
