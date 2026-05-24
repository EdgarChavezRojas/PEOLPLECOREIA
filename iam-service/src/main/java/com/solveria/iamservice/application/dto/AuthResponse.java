package com.solveria.iamservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Successful authentication response containing the issued JWT. */
@Schema(description = "Successful authentication response")
public record AuthResponse(
        @Schema(
                        description = "Signed RS256 JWT bearer token",
                        example = "eyJhbGciOiJSUzI1NiJ9...")
                String accessToken,
        @Schema(description = "Token type, always 'Bearer'", example = "Bearer")
                String tokenType) {

    public AuthResponse(String accessToken) {
        this(accessToken, "Bearer");
    }
}
