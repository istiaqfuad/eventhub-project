package org.istiaqfuad.eventhub.security.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated caller as an {@link AuthenticatedUser} (id + roles),
 * resolved from the {@code SecurityContext} by {@link CurrentUserArgumentResolver}.
 * Use for handlers that make role- or ownership-based authorization decisions;
 * use {@link CurrentUserId} when only the numeric id is needed.
 *
 * <p>The endpoint is expected to sit behind authentication; an anonymous or
 * unauthenticated request is rejected with 401 rather than injecting {@code null}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
