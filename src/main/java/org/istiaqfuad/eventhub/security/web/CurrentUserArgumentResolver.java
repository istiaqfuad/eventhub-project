package org.istiaqfuad.eventhub.security.web;

import org.istiaqfuad.eventhub.security.userdetails.AppUserPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves a {@link CurrentUser}-annotated {@link AuthenticatedUser} parameter from
 * the {@code SecurityContext}: the user id from the principal (a bare {@code Long}
 * for bearer-token requests, or an {@link AppUserPrincipal}), and the role names
 * from the granted authorities with the {@code ROLE_} prefix stripped.
 *
 * <p>Anonymous or unauthenticated requests are rejected with
 * {@link AuthenticationCredentialsNotFoundException} (mapped to 401); the resolver
 * never returns {@code null}.
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && AuthenticatedUser.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user");
        }
        long userId = userId(auth.getPrincipal());
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith(ROLE_PREFIX) ? a.substring(ROLE_PREFIX.length()) : a)
                .collect(Collectors.toUnmodifiableSet());
        return new AuthenticatedUser(userId, roles);
    }

    private static long userId(Object principal) {
        if (principal instanceof Long id) {
            return id;
        }
        if (principal instanceof AppUserPrincipal p) {
            return p.getUserId();
        }
        throw new AuthenticationCredentialsNotFoundException("Unsupported principal type");
    }
}
