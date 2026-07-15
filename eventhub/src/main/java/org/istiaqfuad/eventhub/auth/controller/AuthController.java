package org.istiaqfuad.eventhub.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.istiaqfuad.eventhub.auth.dto.LoginRequest;
import org.istiaqfuad.eventhub.auth.dto.TokenResponse;
import org.istiaqfuad.eventhub.auth.service.AuthService;
import org.istiaqfuad.eventhub.security.CookieProperties;
import org.istiaqfuad.eventhub.security.JwtProperties;
import org.istiaqfuad.eventhub.user.dto.RegisterUserRequest;
import org.istiaqfuad.eventhub.user.dto.UserResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/auth", version = "1")
public class AuthController {

    private static final String COOKIE_PATH = "/api/auth";

    private final AuthService authService;
    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, CookieProperties cookieProperties,
                          JwtProperties jwtProperties) {
        this.authService = authService;
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request);
        addRefreshCookie(response, result.refreshRawToken());
        return ResponseEntity.ok(result.body());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = "${app.security.cookie.refresh-name}", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthService.LoginResult result = authService.refresh(refreshToken);
        addRefreshCookie(response, result.refreshRawToken());
        return ResponseEntity.ok(result.body());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${app.security.cookie.refresh-name}", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        // Clear the refresh cookie (Max-Age=0), same attributes so the browser matches it.
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie("", 0).toString());
        return ResponseEntity.noContent().build();
    }

    /**
     * Appends the refresh cookie directly to the servlet response instead of via the
     * {@code ResponseEntity} headers. The CSRF filter has already added an {@code XSRF-TOKEN}
     * {@code Set-Cookie}; routing this through {@code ResponseEntity} would overwrite it
     * (first value uses replace semantics), leaving the client with no CSRF token.
     */
    private void addRefreshCookie(HttpServletResponse response, String rawToken) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                refreshCookie(rawToken, jwtProperties.refreshTtl().toSeconds()).toString());
    }

    private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(cookieProperties.refreshName(), value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }
}
