package org.istiaqfuad.eventhub.user.service;

import org.istiaqfuad.eventhub.user.dto.RegisterUserRequest;
import org.istiaqfuad.eventhub.user.dto.UserResponse;
import org.istiaqfuad.eventhub.user.entity.User;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Basic user operations. Registration stores the account disabled with no
 * roles; role assignment and password hashing arrive with Spring Security.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public UserResponse register(RegisterUserRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new IllegalStateException("email already registered: " + request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(request.password()); // TODO: hash once Spring Security is wired
        user.setEnabled(false);
        return toResponse(users.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        User user = users.findById(id)
                .orElseThrow(() -> new NoSuchElementException("user not found: " + id));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());
        return new UserResponse(
                user.getId(),
                user.getPublicId(),
                user.getEmail(),
                Boolean.TRUE.equals(user.getEnabled()),
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
