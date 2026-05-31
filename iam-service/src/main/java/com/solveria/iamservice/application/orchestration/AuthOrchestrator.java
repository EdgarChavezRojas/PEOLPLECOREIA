package com.solveria.iamservice.application.orchestration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.solveria.core.iam.application.port.UserRepositoryPort;
import com.solveria.core.iam.domain.model.User;
import com.solveria.iamservice.application.dto.AuthResponse;
import com.solveria.iamservice.application.exception.IamServiceException;
import com.solveria.iamservice.config.security.JwtService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Orchestrator for authentication flows (credential login and Google SSO).
 *
 * <p>Follows the IAM convention: Controller → Orchestrator → Port → Domain → Adapter → DB. This
 * class lives in {@code application.orchestration} and coordinates ports/infrastructure without
 * containing domain logic itself.
 */
public class AuthOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AuthOrchestrator.class);

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public AuthOrchestrator(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleIdTokenVerifier googleIdTokenVerifier) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

    // ── Credential login ──────────────────────────────────────────────────────

    /**
     * Validates credentials and issues a JWT if they are correct.
     *
     * @param identifier email or username supplied by the client
     * @param rawPassword plain-text password supplied by the client
     * @return {@link AuthResponse} containing the signed JWT
     * @throws IamServiceException with code {@code IAM_AUTH_INVALID_CREDENTIALS} on failure
     */
    public AuthResponse login(String identifier, String rawPassword) {
        log.info("event=IAM_AUTH_LOGIN_ATTEMPT identifier={}", identifier);

        User user = resolveUserByIdentifier(identifier);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("event=IAM_AUTH_LOGIN_FAILED reason=BAD_PASSWORD identifier={}", identifier);
            throw new IamServiceException(
                    "IAM_AUTH_INVALID_CREDENTIALS", "Invalid email/username or password");
        }

        if (!user.isActive()) {
            log.warn("event=IAM_AUTH_LOGIN_FAILED reason=USER_INACTIVE userId={}", user.getId());
            throw new IamServiceException("IAM_AUTH_USER_INACTIVE", "User account is inactive");
        }

        String token =
                jwtService.generateToken(user.getId(), user.getTenantId(), user.getRoleIds());

        log.info(
                "event=IAM_AUTH_LOGIN_SUCCESS userId={} tenantId={}",
                user.getId(),
                user.getTenantId());
        return new AuthResponse(token);
    }

    // ── Google SSO ────────────────────────────────────────────────────────────

    /**
     * Validates a Google ID token and issues our own JWT if the email is known.
     *
     * <p>Intentional design: if the email does not exist in our system the call returns 401. User
     * provisioning / signup flows are handled by a separate use case.
     *
     * @param idTokenString the raw Google ID token string from the client
     * @return {@link AuthResponse} containing the signed JWT
     * @throws IamServiceException with code {@code IAM_AUTH_GOOGLE_INVALID_TOKEN} if the Google
     *     token is invalid, or {@code IAM_AUTH_GOOGLE_USER_NOT_FOUND} if the email is unknown
     */
    public AuthResponse googleLogin(String idTokenString) {
        log.info("event=IAM_AUTH_GOOGLE_LOGIN_ATTEMPT");

        GoogleIdToken idToken = verifyGoogleToken(idTokenString);
        String email = idToken.getPayload().getEmail();

        log.debug("event=IAM_AUTH_GOOGLE_TOKEN_VERIFIED email={}", email);

        User user =
                userRepositoryPort
                        .findByEmail(email)
                        .orElseThrow(
                                () -> {
                                    log.warn(
                                            "event=IAM_AUTH_GOOGLE_LOGIN_FAILED reason=USER_NOT_FOUND email={}",
                                            email);
                                    return new IamServiceException(
                                            "IAM_AUTH_GOOGLE_USER_NOT_FOUND",
                                            "No account found for this Google email");
                                });

        if (!user.isActive()) {
            log.warn(
                    "event=IAM_AUTH_GOOGLE_LOGIN_FAILED reason=USER_INACTIVE userId={}",
                    user.getId());
            throw new IamServiceException("IAM_AUTH_USER_INACTIVE", "User account is inactive");
        }

        String token =
                jwtService.generateToken(user.getId(), user.getTenantId(), user.getRoleIds());

        log.info(
                "event=IAM_AUTH_GOOGLE_LOGIN_SUCCESS userId={} tenantId={}",
                user.getId(),
                user.getTenantId());
        return new AuthResponse(token);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Resolves a user by treating the identifier first as email, then as username.
     *
     * <p>This avoids requiring the client to know which field to use.
     */
    private User resolveUserByIdentifier(String identifier) {
        boolean looksLikeEmail = identifier.contains("@");

        if (looksLikeEmail) {
            return userRepositoryPort
                    .findByEmail(identifier)
                    .orElseGet(
                            () ->
                                    userRepositoryPort
                                            .findByUsername(identifier)
                                            .orElseThrow(() -> credentialsException(identifier)));
        } else {
            return userRepositoryPort
                    .findByUsername(identifier)
                    .orElseGet(
                            () ->
                                    userRepositoryPort
                                            .findByEmail(identifier)
                                            .orElseThrow(() -> credentialsException(identifier)));
        }
    }

    private IamServiceException credentialsException(String identifier) {
        log.warn("event=IAM_AUTH_LOGIN_FAILED reason=USER_NOT_FOUND identifier={}", identifier);
        return new IamServiceException(
                "IAM_AUTH_INVALID_CREDENTIALS", "Invalid email/username or password");
    }

    private GoogleIdToken verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
            if (idToken == null) {
                throw new IamServiceException(
                        "IAM_AUTH_GOOGLE_INVALID_TOKEN", "Google ID token verification failed");
            }
            return idToken;
        } catch (GeneralSecurityException | IOException e) {
            log.error("event=IAM_AUTH_GOOGLE_TOKEN_VERIFICATION_ERROR", e);
            throw new IamServiceException(
                    "IAM_AUTH_GOOGLE_INVALID_TOKEN",
                    "Google ID token verification failed: " + e.getMessage(),
                    e);
        }
    }
}
