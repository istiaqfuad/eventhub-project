package org.istiaqfuad.eventhub.auth.service;

import org.istiaqfuad.eventhub.auth.entity.RefreshToken;
import org.istiaqfuad.eventhub.auth.repository.RefreshTokenRepository;
import org.istiaqfuad.eventhub.security.JwtProperties;
import org.istiaqfuad.eventhub.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Opaque refresh tokens: the raw value is 256 bits of randomness handed to the client
 * cookie; only its SHA-256 hash is persisted. Rotation revokes the presented token and
 * issues a fresh one; presenting an already-revoked token is treated as theft and
 * revokes every token for that user.
 */
@Service
@Transactional
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository tokens;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository tokens, JwtProperties jwtProperties) {
        this.tokens = tokens;
        this.jwtProperties = jwtProperties;
    }

    public String issue(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = URL_ENCODER.encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(sha256(raw));
        token.setExpiresAt(OffsetDateTime.now().plus(jwtProperties.refreshTtl()));
        token.setRevoked(false);
        tokens.save(token);
        return raw;
    }

    // Reuse detection revokes the whole token family, then rejects the request. Without
    // noRollbackFor, the rejecting throw would roll back that revocation in the same tx.
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Rotation rotate(String rawToken) {
        RefreshToken stored = tokens.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Unknown refresh token"));

        if (Boolean.TRUE.equals(stored.getRevoked())) {
            // Reuse of an already-rotated token: assume theft, revoke the whole family.
            tokens.revokeAllForUser(stored.getUser().getId());
            throw new InvalidRefreshTokenException("Refresh token reuse detected");
        }
        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        stored.setRevoked(true);
        User user = stored.getUser();
        String newRaw = issue(user);
        return new Rotation(user, newRaw);
    }

    public void revoke(String rawToken) {
        Optional<RefreshToken> stored = tokens.findByTokenHash(sha256(rawToken));
        stored.ifPresent(t -> t.setRevoked(true));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record Rotation(User user, String newRawToken) {
    }
}
