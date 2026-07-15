package org.istiaqfuad.eventhub.auth.service;

import org.istiaqfuad.eventhub.auth.dto.LoginRequest;
import org.istiaqfuad.eventhub.auth.dto.TokenResponse;
import org.istiaqfuad.eventhub.common.exception.DuplicateResourceException;
import org.istiaqfuad.eventhub.security.jwt.JwtService;
import org.istiaqfuad.eventhub.security.userdetails.AppUserPrincipal;
import org.istiaqfuad.eventhub.user.dto.RegisterUserRequest;
import org.istiaqfuad.eventhub.user.dto.UserResponse;
import org.istiaqfuad.eventhub.user.entity.Role;
import org.istiaqfuad.eventhub.user.entity.RoleName;
import org.istiaqfuad.eventhub.user.entity.User;
import org.istiaqfuad.eventhub.user.repository.RoleRepository;
import org.istiaqfuad.eventhub.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestrates registration and the token lifecycle. Registration runs the
 * HaveIBeenPwned compromised-password check (registration only, per design), hashes
 * with bcrypt, assigns the default CUSTOMER role, and enables the account.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
                       CompromisedPasswordChecker compromisedPasswordChecker,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.compromisedPasswordChecker = compromisedPasswordChecker;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponse register(RegisterUserRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new DuplicateResourceException("email already registered: " + request.email());
        }
        if (compromisedPasswordChecker.check(request.password()).isCompromised()) {
            throw new CompromisedPasswordException(
                    "The provided password appears in a known data breach; choose another.");
        }
        Role customer = roles.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not seeded"));

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(Set.of(customer));
        return toResponse(users.save(user));
    }

    public LoginResult login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
        User user = users.getReferenceById(principal.getUserId());
        String access = jwtService.generateAccessToken(
                principal.getUserId(), principal.getEmail(), principal.getRoleNames());
        String refreshRaw = refreshTokenService.issue(user);
        return new LoginResult(tokenResponse(access), refreshRaw);
    }

    // Owns the physical transaction for the refresh path; must not roll back the
    // reuse-detection revocation performed inside RefreshTokenService.rotate.
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public LoginResult refresh(String rawToken) {
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawToken);
        User user = rotation.user();
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleNames);
        return new LoginResult(tokenResponse(access), rotation.newRawToken());
    }

    public void logout(String rawToken) {
        if (rawToken != null) {
            refreshTokenService.revoke(rawToken);
        }
    }

    private TokenResponse tokenResponse(String accessToken) {
        return new TokenResponse(accessToken, "Bearer", jwtService.accessTtl().toSeconds());
    }

    private UserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());
        return new UserResponse(user.getId(), user.getPublicId(), user.getEmail(),
                Boolean.TRUE.equals(user.getEnabled()), roleNames,
                user.getCreatedAt(), user.getUpdatedAt());
    }

    public record LoginResult(TokenResponse body, String refreshRawToken) {
    }
}
