package org.istiaqfuad.eventhub.security.web;

import java.util.Set;

/**
 * The authenticated caller in a form the domain layer can make authorization
 * decisions with: the numeric user id plus the caller's role names (without the
 * {@code ROLE_} prefix). Resolved by {@link CurrentUserArgumentResolver} and
 * injected via {@link CurrentUser}.
 *
 * <p>Use this where a handler needs more than identity — role gating that the
 * service performs, or owner-or-admin ownership checks. When only the buyer id
 * is needed, prefer {@link CurrentUserId}.
 */
public record AuthenticatedUser(long id, Set<String> roles) {

    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
