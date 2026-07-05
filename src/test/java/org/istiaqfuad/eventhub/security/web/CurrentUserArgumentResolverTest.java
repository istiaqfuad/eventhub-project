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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unused")
    void annotated(@CurrentUser AuthenticatedUser user) {
    }

    @SuppressWarnings("unused")
    void plain(AuthenticatedUser user) {
    }

    private static MethodParameter param(String method) throws NoSuchMethodException {
        Method m = CurrentUserArgumentResolverTest.class.getDeclaredMethod(method, AuthenticatedUser.class);
        return new MethodParameter(m, 0);
    }

    private static void authenticate(org.springframework.security.core.Authentication auth) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void supportsAnnotatedAuthenticatedUserOnly() throws Exception {
        assertThat(resolver.supportsParameter(param("annotated"))).isTrue();
        assertThat(resolver.supportsParameter(param("plain"))).isFalse();
    }

    @Test
    void resolvesIdAndRolesStrippingRolePrefix() throws Exception {
        authenticate(JwtAuthenticationToken.authenticated(
                7L, AuthorityUtils.createAuthorityList("ROLE_ORGANIZER", "ROLE_ADMIN")));

        AuthenticatedUser user = (AuthenticatedUser) resolver.resolveArgument(param("annotated"), null, null, null);

        assertThat(user.id()).isEqualTo(7L);
        assertThat(user.roles()).containsExactlyInAnyOrder("ORGANIZER", "ADMIN");
        assertThat(user.isAdmin()).isTrue();
        assertThat(user.hasRole("ORGANIZER")).isTrue();
    }

    @Test
    void nonAdminHasNoAdminRole() throws Exception {
        authenticate(JwtAuthenticationToken.authenticated(
                3L, AuthorityUtils.createAuthorityList("ROLE_CUSTOMER")));

        AuthenticatedUser user = (AuthenticatedUser) resolver.resolveArgument(param("annotated"), null, null, null);

        assertThat(user.isAdmin()).isFalse();
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
}
