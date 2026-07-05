package org.istiaqfuad.eventhub.security.userdetails;

import lombok.Getter;
import lombok.NonNull;
import org.istiaqfuad.eventhub.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapts a {@link User} to Spring Security's {@link UserDetails}. Authorities are
 * {@code ROLE_<name>}. Exposes the numeric user id for JWT subject / auditing.
 */
public final class AppUserPrincipal implements UserDetails {

    @Getter
    private final long userId;
    @Getter
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    @Getter
    private final Set<String> roleNames;

    public AppUserPrincipal(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.enabled = Boolean.TRUE.equals(user.getEnabled());
        this.roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return roleNames.stream()
                .map(name -> new SimpleGrantedAuthority("ROLE_" + name))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
