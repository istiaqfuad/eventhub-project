package org.istiaqfuad.eventhub.auth.service;

/** Thrown when a presented refresh token is missing, expired, or already revoked. */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
