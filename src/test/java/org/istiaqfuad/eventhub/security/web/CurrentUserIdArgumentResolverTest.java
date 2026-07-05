package org.istiaqfuad.eventhub.security.web;

import org.istiaqfuad.eventhub.security.jwt.JwtAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserIdArgumentResolverTest {

    private final CurrentUserIdArgumentResolver resolver = new CurrentUserIdArgumentResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unused")
    void annotated(@CurrentUserId Long userId) {
    }

    @SuppressWarnings("unused")
    void plain(Long userId) {
    }

    private static MethodParameter param(String method) throws NoSuchMethodException {
        Method m = CurrentUserIdArgumentResolverTest.class.getDeclaredMethod(method, Long.class);
        return new MethodParameter(m, 0);
    }

    @Test
    void supportsAnnotatedLongOnly() throws Exception {
        assertThat(resolver.supportsParameter(param("annotated"))).isTrue();
        assertThat(resolver.supportsParameter(param("plain"))).isFalse();
    }

    @Test
    void resolvesUserIdFromBearerPrincipal() throws Exception {
        authenticate(JwtAuthenticationToken.authenticated(42L, AuthorityUtils.NO_AUTHORITIES));

        Object resolved = resolver.resolveArgument(param("annotated"), null, null, null);

        assertThat(resolved).isEqualTo(42L);
    }

    @Test
    void rejectsWhenUnauthenticated() throws Exception {
        assertThatThrownBy(() -> resolver.resolveArgument(param("annotated"), null, null, null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void rejectsAnonymous() throws Exception {
        authenticate(new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThatThrownBy(() -> resolver.resolveArgument(param("annotated"), null, null, null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private static void authenticate(org.springframework.security.core.Authentication auth) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}
