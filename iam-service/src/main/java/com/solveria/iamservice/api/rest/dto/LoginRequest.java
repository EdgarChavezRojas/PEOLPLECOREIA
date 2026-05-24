package com.solveria.iamservice.api.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the standard credential-based login endpoint.
 *
 * <p>The {@code identifier} field accepts either an email address or a username.
 */
@Schema(description = "Credential-based login request")
public record LoginRequest(
        @Schema(
                        description = "Email address or username of the user",
                        example = "user@solveria.com",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "identifier must not be blank")
                String identifier,
        @Schema(
                        description = "User password",
                        example = "s3cr3tP@ss!",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "password must not be blank")
                String password) {}
