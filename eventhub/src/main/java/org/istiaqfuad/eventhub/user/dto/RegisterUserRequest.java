package org.istiaqfuad.eventhub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sign-up payload. The raw {@code password} is hashed server-side into
 * {@code User.passwordHash}; {@code enabled} and roles are assigned by the
 * service, never trusted from the client.
 */
public record RegisterUserRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email must not exceed 255 characters")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password
) {
}
