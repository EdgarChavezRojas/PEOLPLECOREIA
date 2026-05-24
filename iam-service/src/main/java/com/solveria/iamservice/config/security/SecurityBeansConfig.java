package com.solveria.iamservice.config.security;

import com.solveria.core.security.filter.SecurityContextFilter;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Wires the security infrastructure beans that are shared across both filter chains.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Registers {@link JwtService} as a Spring bean (loaded once at startup).
 *   <li>Registers {@link BCryptPasswordEncoder} as the application's {@link PasswordEncoder}.
 *   <li>Registers {@link SecurityContextFilter} with its concrete {@code JwtClaimsExtractor}
 *       lambda, keeping {@code core-platform} free of Nimbus/JOSE dependencies.
 *   <li>Registers {@link com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier}
 *       pre-configured with the project's Google client ID.
 * </ul>
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, GoogleProperties.class})
public class SecurityBeansConfig {

    private final JwtProperties jwtProperties;
    private final GoogleProperties googleProperties;

    public SecurityBeansConfig(JwtProperties jwtProperties, GoogleProperties googleProperties) {
        this.jwtProperties = jwtProperties;
        this.googleProperties = googleProperties;
    }

    /**
     * RSA-based stateless JWT service.
     *
     * <p>Keys are loaded from the paths declared in {@code security.jwt.*} properties.
     */
    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtProperties);
    }

    /** BCrypt password encoder used for login credential validation. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security context filter that reads the JWT bearer token, validates it, and injects
     * {@code tenantId} / {@code userId} into thread-local contexts.
     *
     * <p>The lambda adapter bridges the {@code core-platform} {@link SecurityContextFilter.JwtClaimsExtractor}
     * interface to the concrete {@link JwtService} without creating a compile-time dependency from
     * {@code core-platform} on the Nimbus-JOSE library.
     */
    @Bean
    public SecurityContextFilter securityContextFilter(JwtService jwtService) {
        SecurityContextFilter.JwtClaimsExtractor extractor = token -> {
            JWTClaimsSet claims = jwtService.validateAndExtractClaims(token);
            String tenantId = jwtService.extractTenantId(claims);
            Long userId = jwtService.extractUserId(claims);
            return new SecurityContextFilter.Claims(tenantId, userId);
        };
        return new SecurityContextFilter(extractor);
    }

    /**
     * Google ID token verifier pre-configured with the project's OAuth2 client ID.
     *
     * <p>Used by {@link com.solveria.iamservice.application.orchestration.AuthOrchestrator} to
     * validate Google sign-in tokens without any database access.
     */
    @Bean
    public com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                        new com.google.api.client.http.javanet.NetHttpTransport(),
                        new com.google.api.client.json.gson.GsonFactory())
                .setAudience(java.util.List.of(googleProperties.clientId()))
                .build();
    }
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("password"));
    }
}
