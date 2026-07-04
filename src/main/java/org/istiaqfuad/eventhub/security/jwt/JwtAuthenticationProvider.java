package org.istiaqfuad.eventhub.security.jwt;

import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authenticates a bearer token: validates signature/expiry/issuer via
 * {@link JwtService} and builds authorities straight from the {@code roles} claim.
 * No database access — the signed token is the source of truth.
 */
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;

    public JwtAuthenticationProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        try {
            JwtService.ParsedToken parsed = jwtService.parse(token);
            List<SimpleGrantedAuthority> authorities = parsed.roleNames().stream()
                    .map(name -> new SimpleGrantedAuthority("ROLE_" + name))
                    .toList();
            return JwtAuthenticationToken.authenticated(parsed.userId(), authorities);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid or expired token", ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
