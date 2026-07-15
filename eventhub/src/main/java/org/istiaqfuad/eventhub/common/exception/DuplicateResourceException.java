package org.istiaqfuad.eventhub.common.exception;

/**
 * Thrown when creating a resource that would violate a uniqueness rule
 * (duplicate email, a second review for the same event, …). Mapped to HTTP
 * 409 by {@link GlobalExceptionHandler}.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
