package org.istiaqfuad.eventhub.user.controller;

import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.user.dto.RegisterUserRequest;
import org.istiaqfuad.eventhub.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Stub controller for wiring/validation testing only. Returns canned
 * responses with no persistence; replace with service-backed logic later.
 */
@RestController
@RequestMapping(path = "/users", version = "1")
public class UserController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        return new UserResponse(1L, UUID.randomUUID(), request.email(), false,
                Set.of("CUSTOMER"), now, now);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        return new UserResponse(id, UUID.randomUUID(), "stub@example.com", true,
                Set.of("CUSTOMER"), now, now);
    }
}
