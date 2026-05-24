package com.solveria.iamservice.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Google OAuth2 ID token verification.
 *
 * <p>Mapped from {@code google.client-id} in application.yml.
 */
@ConfigurationProperties(prefix = "google")
public record GoogleProperties(
        /** Google OAuth2 client ID used to verify ID tokens. */
        String clientId) {}
