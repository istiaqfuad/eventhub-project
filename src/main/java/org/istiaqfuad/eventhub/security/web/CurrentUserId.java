package org.istiaqfuad.eventhub.security.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated caller's numeric user id into a controller handler
 * parameter, resolved from the {@code SecurityContext} by
 * {@link CurrentUserIdArgumentResolver}. Use instead of trusting a client-supplied
 * id, so a request can only ever act as its own authenticated user.
 *
 * <p>The annotated parameter must be a {@code Long}. The endpoint is expected to sit
 * behind authentication; if no authenticated user is present the resolver rejects
 * the request with 401 rather than injecting {@code null}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
