package com.solveria.iamservice.api.rest;

import com.solveria.iamservice.api.exception.dto.ApiErrorResponse;
import com.solveria.iamservice.api.rest.dto.GoogleLoginRequest;
import com.solveria.iamservice.api.rest.dto.LoginRequest;
import com.solveria.iamservice.application.dto.AuthResponse;
import com.solveria.iamservice.application.orchestration.AuthOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller exposing login and Google SSO endpoints.
 *
 * <p>Both endpoints are publicly accessible (no JWT required). On success, they return a signed
 * RS256 JWT that the client must include as {@code Authorization: Bearer <token>} on subsequent
 * requests.
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "Login and token issuance operations")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthOrchestrator authOrchestrator;

    public AuthController(AuthOrchestrator authOrchestrator) {
        this.authOrchestrator = authOrchestrator;
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(
            operationId = "login",
            summary = "Credential-based login",
            description =
                    "Authenticates a user with email (or username) and password. Returns a signed"
                            + " RS256 JWT on success.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Login successful",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AuthResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Success",
                                                        value =
                                                                """
                                                            {
                                                              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
                                                              "tokenType": "Bearer"
                                                            }
                                                            """))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Invalid credentials",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid credentials",
                                                        value =
                                                                """
                                                            {
                                                              "errorCode": "IAM_AUTH_INVALID_CREDENTIALS",
                                                              "timestamp": "2024-01-15T10:30:00Z",
                                                              "path": "/api/auth/login"
                                                            }
                                                            """)))
            })
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request) {
        log.debug(
                "event=IAM_AUTH_LOGIN_HTTP_REQUEST_RECEIVED identifier={}",
                request.identifier());

        AuthResponse response = authOrchestrator.login(request.identifier(), request.password());
        return ResponseEntity.ok(response);
    }

    // ── POST /api/auth/google ─────────────────────────────────────────────────

    @PostMapping("/google")
    @Operation(
            operationId = "googleLogin",
            summary = "Google SSO login",
            description =
                    "Validates a Google ID token. If the associated email exists in the system,"
                            + " returns a signed RS256 JWT. Returns 401 if the email is not"
                            + " registered.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Google login successful",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AuthResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Success",
                                                        value =
                                                                """
                                                            {
                                                              "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
                                                              "tokenType": "Bearer"
                                                            }
                                                            """))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Invalid Google token or email not registered",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class),
                                        examples = {
                                            @ExampleObject(
                                                    name = "Invalid token",
                                                    value =
                                                            """
                                                    {
                                                      "errorCode": "IAM_AUTH_GOOGLE_INVALID_TOKEN",
                                                      "timestamp": "2024-01-15T10:30:00Z",
                                                      "path": "/api/auth/google"
                                                    }
                                                    """),
                                            @ExampleObject(
                                                    name = "User not found",
                                                    value =
                                                            """
                                                    {
                                                      "errorCode": "IAM_AUTH_GOOGLE_USER_NOT_FOUND",
                                                      "timestamp": "2024-01-15T10:30:00Z",
                                                      "path": "/api/auth/google"
                                                    }
                                                    """)
                                        }))
            })
    public ResponseEntity<AuthResponse> googleLogin(
            @RequestBody @Valid GoogleLoginRequest request) {
        log.debug("event=IAM_AUTH_GOOGLE_LOGIN_HTTP_REQUEST_RECEIVED");

        AuthResponse response = authOrchestrator.googleLogin(request.idToken());
        return ResponseEntity.ok(response);
    }
}
