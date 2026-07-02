package org.istiaqfuad.eventhub.review.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.review.dto.ReviewRequest;
import org.istiaqfuad.eventhub.review.dto.ReviewResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * Stub controller for wiring/validation testing only. Returns canned
 * responses with no persistence; replace with service-backed logic later.
 */
@RestController
@RequestMapping(path = "/reviews", version = "1")
public class ReviewController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        return new ReviewResponse(1L, request.eventId(), 1L,
                request.rating(), request.body(), now, now);
    }

    @GetMapping("/{id}")
    public ReviewResponse get(@PathVariable Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        return new ReviewResponse(id, 1L, 1L, 5, "Stub review body", now, now);
    }
}
