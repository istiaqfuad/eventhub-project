package org.istiaqfuad.eventhub.user.service;

import org.istiaqfuad.eventhub.security.web.AuthenticatedUser;
import org.istiaqfuad.eventhub.user.entity.User;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Self-or-admin enforcement on {@link UserService#get}. */
class UserServiceTest {

    private UserRepository users;
    private UserService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        service = new UserService(users);

        User u = new User();
        u.setId(10L);
        u.setEmail("a@b.c");
        u.setEnabled(true);
        u.setRoles(Set.of());
        when(users.findById(10L)).thenReturn(Optional.of(u));
    }

    private static AuthenticatedUser user(long id, String... roles) {
        return new AuthenticatedUser(id, Set.of(roles));
    }

    @Test
    void userCanReadOwnProfile() {
        assertThat(service.get(10L, user(10L)).id()).isEqualTo(10L);
    }

    @Test
    void readingAnotherProfileIsDenied() {
        assertThatThrownBy(() -> service.get(10L, user(11L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanReadAnyProfile() {
        assertThat(service.get(10L, user(99L, "ADMIN")).id()).isEqualTo(10L);
    }
}
