package org.istiaqfuad.eventhub.security.jwt;

import io.jsonwebtoken.JwtException;
import org.istiaqfuad.eventhub.security.JwtProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 32-byte Base64 key (>=256 bits) for HS256
    private static final String SECRET = "aG9sZC1teS1iZWVyLXNlY3JldC1rZXktMzIteXk9PT0=";

    private JwtService service(Duration ttl) {
        return new JwtService(new JwtProperties(SECRET, "eventhub", ttl, Duration.ofDays(30)));
    }

    @Test
    void round_trips_subject_email_and_roles() {
        JwtService jwt = service(Duration.ofMinutes(15));

        String token = jwt.generateAccessToken(42L, "a@b.com", List.of("CUSTOMER", "ADMIN"));
        JwtService.ParsedToken parsed = jwt.parse(token);

        assertThat(parsed.userId()).isEqualTo(42L);
        assertThat(parsed.email()).isEqualTo("a@b.com");
        assertThat(parsed.roleNames()).containsExactlyInAnyOrder("CUSTOMER", "ADMIN");
    }

    @Test
    void rejects_expired_token() {
        JwtService jwt = service(Duration.ofSeconds(-1)); // already expired
        String token = jwt.generateAccessToken(1L, "x@y.com", List.of("CUSTOMER"));

        assertThatThrownBy(() -> jwt.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejects_tampered_signature() {
        JwtService jwt = service(Duration.ofMinutes(15));
        String token = jwt.generateAccessToken(1L, "x@y.com", List.of("CUSTOMER"));
        String tampered = token.substring(0, token.length() - 2)
                + (token.endsWith("aa") ? "bb" : "aa");

        assertThatThrownBy(() -> jwt.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejects_wrong_issuer() {
        JwtService signer = new JwtService(
                new JwtProperties(SECRET, "evil", Duration.ofMinutes(15), Duration.ofDays(30)));
        String token = signer.generateAccessToken(1L, "x@y.com", List.of("CUSTOMER"));

        JwtService verifier = service(Duration.ofMinutes(15)); // issuer "eventhub"
        assertThatThrownBy(() -> verifier.parse(token)).isInstanceOf(JwtException.class);
    }
}
