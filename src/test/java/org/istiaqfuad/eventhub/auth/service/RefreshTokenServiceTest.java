package org.istiaqfuad.eventhub.auth.service;

import org.istiaqfuad.eventhub.auth.entity.RefreshToken;
import org.istiaqfuad.eventhub.auth.repository.RefreshTokenRepository;
import org.istiaqfuad.eventhub.security.JwtProperties;
import org.istiaqfuad.eventhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private RefreshTokenRepository repo;
    private RefreshTokenService service;

    private User user(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @BeforeEach
    void setUp() {
        repo = mock(RefreshTokenRepository.class);
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        JwtProperties props = new JwtProperties("x", "eventhub",
                Duration.ofMinutes(15), Duration.ofDays(30));
        service = new RefreshTokenService(repo, props);
    }

    @Test
    void issue_persists_hash_not_raw_and_returns_raw() {
        String raw = service.issue(user(7L));

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repo).save(saved.capture());
        assertThat(raw).isNotBlank();
        assertThat(saved.getValue().getTokenHash()).isNotEqualTo(raw); // stored hashed
        assertThat(saved.getValue().getRevoked()).isFalse();
        assertThat(saved.getValue().getExpiresAt()).isAfter(OffsetDateTime.now());
    }

    @Test
    void rotate_happy_path_revokes_old_and_issues_new() {
        String raw = service.issue(user(7L));
        ArgumentCaptor<RefreshToken> first = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repo).save(first.capture());
        RefreshToken stored = first.getValue();
        stored.setUser(user(7L));
        stored.setExpiresAt(OffsetDateTime.now().plusDays(1));
        stored.setRevoked(false);
        when(repo.findByTokenHash(stored.getTokenHash())).thenReturn(Optional.of(stored));

        RefreshTokenService.Rotation rotation = service.rotate(raw);

        assertThat(stored.getRevoked()).isTrue();          // old revoked
        assertThat(rotation.newRawToken()).isNotBlank();
        assertThat(rotation.user().getId()).isEqualTo(7L);
    }

    @Test
    void rotate_unknown_token_throws() {
        when(repo.findByTokenHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.rotate("nope"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rotate_expired_token_throws() {
        String raw = service.issue(user(7L));
        ArgumentCaptor<RefreshToken> c = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repo).save(c.capture());
        RefreshToken stored = c.getValue();
        stored.setUser(user(7L));
        stored.setExpiresAt(OffsetDateTime.now().minusSeconds(1)); // expired
        stored.setRevoked(false);
        when(repo.findByTokenHash(stored.getTokenHash())).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.rotate(raw))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rotate_revoked_token_is_reuse_and_revokes_all() {
        String raw = service.issue(user(7L));
        ArgumentCaptor<RefreshToken> c = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repo).save(c.capture());
        RefreshToken stored = c.getValue();
        stored.setUser(user(7L));
        stored.setExpiresAt(OffsetDateTime.now().plusDays(1));
        stored.setRevoked(true); // already used -> reuse
        when(repo.findByTokenHash(stored.getTokenHash())).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.rotate(raw))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(repo).revokeAllForUser(7L);
    }

    @Test
    void revoke_unknown_token_is_noop() {
        when(repo.findByTokenHash(any())).thenReturn(Optional.empty());
        service.revoke("whatever");
        verify(repo, never()).save(any());
    }
}
