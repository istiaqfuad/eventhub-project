package org.istiaqfuad.eventhub.common.exception;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.istiaqfuad.eventhub.auth.service.InvalidRefreshTokenException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Central exception-to-response mapping. Every error leaves the API as an
 * RFC 9457 {@code application/problem+json} document. Custom domain
 * exceptions map to their intended status here; all built-in Spring MVC
 * exceptions (unreadable body, type mismatch, unsupported media/version, …)
 * are turned into problem details by the {@link ResponseEntityExceptionHandler}
 * base class. Every response carries a stable {@code code} and a
 * {@code timestamp}; validation failures also carry an {@code errors} array.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND");
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage(), "DUPLICATE_RESOURCE");
    }

    /**
     * A database constraint fired that the service layer did not pre-check —
     * most often a missing foreign-key reference or a unique-index clash. The
     * SQL cause is logged but never leaked to the client.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT,
                "The request violates a data constraint: a referenced record is missing "
                        + "or a unique value already exists.",
                "CONSTRAINT_VIOLATION");
    }

    /**
     * Registration with a password found in a known breach. Declared before the broader
     * {@link AuthenticationException} handler; Spring picks the most specific match, so
     * compromised passwords map to 400 while every other auth failure maps to 401.
     */
    @ExceptionHandler(CompromisedPasswordException.class)
    public ProblemDetail handleCompromisedPassword(CompromisedPasswordException ex) {
        return problem(HttpStatus.BAD_REQUEST,
                "The provided password appears in a known data breach; choose another.",
                "COMPROMISED_PASSWORD");
    }

    /** Login failures (bad password, disabled account) — never reveal which. */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Invalid email or password.", "INVALID_CREDENTIALS");
    }

    /**
     * Authorization failure for an already-authenticated caller — a role gate
     * ({@code @PreAuthorize}) or an ownership check in the service layer. Declared
     * explicitly so it maps to 403 instead of being swallowed as 500 by the
     * catch-all {@link Exception} handler; message is deliberately generic so it
     * does not reveal whether the target resource exists.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.", "ACCESS_DENIED");
    }

    /** Refresh token missing from store, expired, or replayed after rotation. */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return problem(HttpStatus.UNAUTHORIZED,
                "The refresh token is invalid or expired. Please sign in again.",
                "INVALID_REFRESH_TOKEN");
    }

    /** Last-resort handler so nothing escapes as a raw stack trace. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", "INTERNAL_ERROR");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields.", "VALIDATION_ERROR");
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .toList();
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST,
                "Validation failed for one or more request parameters.", "VALIDATION_ERROR");
        List<String> errors = ex.getAllErrors().stream()
                .map(e -> e.getDefaultMessage() == null ? "invalid" : e.getDefaultMessage())
                .toList();
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * Stamps a {@code timestamp} onto every problem detail the base class
     * produces, so built-in and custom responses share the same shape.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex, @Nullable Object body, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
        if (body instanceof ProblemDetail pd) {
            Map<String, Object> props = pd.getProperties();
            if (props == null || !props.containsKey("timestamp")) {
                pd.setProperty("timestamp", Instant.now());
            }
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ProblemDetail problem(HttpStatus status, String detail, String code) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setProperty("code", code);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
