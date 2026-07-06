package org.istiaqfuad.eventhub.security;

import org.springframework.http.HttpMethod;

/**
 * Single source of truth for the public/private boundary. Anything not listed here is
 * authenticated. Paths include the {@code /api} prefix that {@code WebMvcConfig} adds.
 *
 * <p>{@code /auth/refresh} and {@code /auth/logout} are public at the authorization
 * layer: the refresh <em>cookie</em> authenticates them inside the service, and CSRF
 * (see {@code SecurityConfig}) guards them.
 */
public final class SecurityPaths {

    private SecurityPaths() {
    }

    public record PublicEndpoint(HttpMethod method, String pattern) {
    }

    /** Public, unauthenticated endpoints. A null method matches any HTTP method. */
    public static final PublicEndpoint[] PUBLIC = {
            new PublicEndpoint(HttpMethod.POST, "/api/auth/register"),
            new PublicEndpoint(HttpMethod.POST, "/api/auth/login"),
            new PublicEndpoint(HttpMethod.POST, "/api/auth/refresh"),
            new PublicEndpoint(HttpMethod.POST, "/api/auth/logout"),
            // Stripe calls this with no bearer; it is authenticated by the webhook signature, not the filter chain.
            new PublicEndpoint(HttpMethod.POST, "/api/payments/webhook"),
            new PublicEndpoint(HttpMethod.GET, "/api/events/**"),
            new PublicEndpoint(HttpMethod.GET, "/api/venues/**"),
            new PublicEndpoint(HttpMethod.GET, "/api/reviews/**"),
            new PublicEndpoint(HttpMethod.GET, "/api/version"),
            new PublicEndpoint(null, "/error"),
            new PublicEndpoint(HttpMethod.GET, "/actuator/health"),
            // OpenAPI / Swagger UI
            new PublicEndpoint(HttpMethod.GET, "/v3/api-docs/**"),
            new PublicEndpoint(HttpMethod.GET, "/swagger-ui/**"),
            new PublicEndpoint(HttpMethod.GET, "/swagger-ui.html"),
    };
}
