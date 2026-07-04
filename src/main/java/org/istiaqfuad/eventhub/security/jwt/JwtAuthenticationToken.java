package org.istiaqfuad.eventhub.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Two states:
 * <ul>
 *   <li><b>unauthenticated</b> — carries the raw bearer token as credentials.</li>
 *   <li><b>authenticated</b> — principal is the numeric user id; authorities set.</li>
 * </ul>
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String token;

    private JwtAuthenticationToken(String token) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = null;
        this.token = token;
        setAuthenticated(false);
    }

    private JwtAuthenticationToken(Long userId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = userId;
        this.token = null;
        super.setAuthenticated(true);
    }

    public static JwtAuthenticationToken unauthenticated(String token) {
        return new JwtAuthenticationToken(token);
    }

    public static JwtAuthenticationToken authenticated(
            Long userId, Collection<? extends GrantedAuthority> authorities) {
        return new JwtAuthenticationToken(userId, authorities);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
