package org.istiaqfuad.eventhub.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts {@code Authorization: Bearer <jwt>}, delegates to the
 * {@link AuthenticationManager} (which routes to {@link JwtAuthenticationProvider}),
 * and populates the {@link SecurityContext}. No header → anonymous, chain continues.
 * A present-but-invalid token is rejected immediately via the entry point (401).
 *
 * <p>Constructed directly in {@code SecurityConfig} (not a Spring bean) so Boot does
 * not also register it as a plain servlet filter outside the security chain.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final AuthenticationEntryPoint entryPoint;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   AuthenticationEntryPoint entryPoint) {
        this.authenticationManager = authenticationManager;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(PREFIX.length());
        try {
            Authentication auth = authenticationManager.authenticate(
                    JwtAuthenticationToken.unauthenticated(token));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, ex);
        }
    }
}
