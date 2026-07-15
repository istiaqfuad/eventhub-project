package org.istiaqfuad.eventhub.user.service;

import org.istiaqfuad.eventhub.common.exception.ResourceNotFoundException;
import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.dto.UserResponse;
import org.istiaqfuad.eventhub.user.entity.User;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/** Read-side user operations. Registration lives in the auth module (AuthService). */
@Service
@Transactional
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id, AuthenticatedUser caller) {
        if (!caller.isAdmin() && !id.equals(caller.id())) {
            throw new AccessDeniedException("Cannot read another user's profile");
        }
        User user = users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthenticatedUser caller) {
        User user = users.findById(caller.id())
                .orElseThrow(() -> new ResourceNotFoundException("User", caller.id()));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());
        return new UserResponse(user.getId(), user.getPublicId(), user.getEmail(),
                Boolean.TRUE.equals(user.getEnabled()), roleNames,
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
