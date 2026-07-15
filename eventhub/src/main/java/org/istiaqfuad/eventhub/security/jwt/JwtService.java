package org.istiaqfuad.eventhub.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.istiaqfuad.eventhub.security.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Issues and verifies HS256 access tokens. Verification is stateless: the
 * {@code roles} claim carries authorities, so no database read is needed per request.
 */
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTtl;

    public JwtService(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
        this.issuer = props.issuer();
        this.accessTtl = props.accessTtl();
    }

    public Duration accessTtl() {
        return accessTtl;
    }

    public String generateAccessToken(long userId, String email, Collection<String> roleNames) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(Long.toString(userId))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLES, List.copyOf(roleNames))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .signWith(key)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public ParsedToken parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        long userId = Long.parseLong(claims.getSubject());
        String email = claims.get(CLAIM_EMAIL, String.class);
        List<String> roles = claims.get(CLAIM_ROLES, List.class);
        return new ParsedToken(userId, email, roles == null ? List.of() : List.copyOf(roles));
    }

    public record ParsedToken(long userId, String email, List<String> roleNames) {
    }
}
