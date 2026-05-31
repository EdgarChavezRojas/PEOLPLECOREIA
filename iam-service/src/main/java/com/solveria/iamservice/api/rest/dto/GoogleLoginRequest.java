package com.solveria.iamservice.api.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request body for the Google ID token login endpoint. */
@Schema(description = "Google OAuth2 ID token login request")
public record GoogleLoginRequest(
        @Schema(
                        description =
                                "Google ID token obtained from the client-side Google Sign-In flow",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "idToken must not be blank")
                String idToken) {}
