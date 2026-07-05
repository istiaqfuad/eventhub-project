package org.istiaqfuad.eventhub.security.web;

import org.istiaqfuad.eventhub.security.userdetails.AppUserPrincipal;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves a {@link CurrentUserId}-annotated {@code Long} parameter to the
 * authenticated caller's user id, read from the {@code SecurityContext}.
 *
 * <p>Two principal shapes are accepted, matching {@code AuditingConfig}: a bare
 * {@code Long} (bearer-token requests, the normal path) and an
 * {@link AppUserPrincipal} (password authentication). Anything else — anonymous,
 * unauthenticated, or an unexpected principal type — is rejected with
 * {@link AuthenticationCredentialsNotFoundException}, which the security exception
 * translation maps to a 401 problem detail. The resolver never returns {@code null}.
 */
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long id) {
            return id;
        }
        if (principal instanceof AppUserPrincipal p) {
            return p.getUserId();
        }
        throw new AuthenticationCredentialsNotFoundException("Unsupported principal type");
    }
}
