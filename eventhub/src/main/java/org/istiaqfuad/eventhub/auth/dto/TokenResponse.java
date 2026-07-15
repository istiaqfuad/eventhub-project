package org.istiaqfuad.eventhub.auth.dto;

/** Access-token response body. The refresh token travels in an httpOnly cookie. */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
