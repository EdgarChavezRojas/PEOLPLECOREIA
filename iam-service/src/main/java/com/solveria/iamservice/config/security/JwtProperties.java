package com.solveria.iamservice.config.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT security.
 *
 * <p>When enabled=true, the service requires JWT authentication for /api/** endpoints. When
 * enabled=false (default), all endpoints are accessible without authentication (DEV mode).
 *
 * <p>JWT signing uses asymmetric RSA keys loaded from the paths below: -
 * security.jwt.public-key-path : classpath or file path to the RSA public key (PEM) -
 * security.jwt.private-key-path : classpath or file path to the RSA private key (PEM) -
 * security.jwt.expiration : token validity duration (default 1h)
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        /** Enable JWT authentication. Default: false (DEV mode without auth). */
        boolean enabled,

        /** Path to RSA public key PEM file (classpath: or file: prefix). */
        String publicKeyPath,

        /** Path to RSA private key PEM file (classpath: or file: prefix). */
        String privateKeyPath,

        /** Token validity duration. Default: 1 hour. */
        Duration expiration) {

    public Duration expiration() {
        return expiration != null ? expiration : Duration.ofHours(1);
    }
}
