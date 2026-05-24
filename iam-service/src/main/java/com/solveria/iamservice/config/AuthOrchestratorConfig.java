package com.solveria.iamservice.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.solveria.core.iam.application.port.UserRepositoryPort;
import com.solveria.iamservice.application.orchestration.AuthOrchestrator;
import com.solveria.iamservice.config.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring wiring for {@link AuthOrchestrator}.
 *
 * <p>The orchestrator itself has no Spring annotations (per IAM architecture conventions). Wiring
 * is done here in the config layer.
 */
@Configuration
public class AuthOrchestratorConfig {

    @Bean
    public AuthOrchestrator authOrchestrator(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleIdTokenVerifier googleIdTokenVerifier) {
        return new AuthOrchestrator(
                userRepositoryPort, passwordEncoder, jwtService, googleIdTokenVerifier);
    }
}
