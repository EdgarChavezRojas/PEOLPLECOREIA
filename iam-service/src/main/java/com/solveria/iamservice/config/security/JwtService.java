package com.solveria.iamservice.config.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Stateless JWT service using asymmetric RSA keys (RS256).
 *
 * <p>Generates tokens with mandatory claims: tenantId, userId, roleIds. Validation is performed
 * locally using the RSA public key — zero database calls.
 *
 * <p>Keys are loaded once at startup from PEM files referenced in {@link JwtProperties}. Supports
 * both {@code classpath:} and {@code file:} resource prefixes.
 */
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final String CLAIM_TENANT_ID = "tenantId";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE_IDS = "roleIds";
    private static final String ISSUER = "solveria-iam";

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final Duration expiration;

    public JwtService(JwtProperties jwtProperties) {
        this.expiration = jwtProperties.expiration();
        try {
            this.privateKey = loadPrivateKey(jwtProperties.privateKeyPath());
            this.publicKey = loadPublicKey(jwtProperties.publicKeyPath());
            log.info("event=JWT_SERVICE_INITIALIZED algorithm=RS256");
        } catch (IOException e) {
            throw new IllegalStateException(
                    "event=JWT_SERVICE_KEY_LOAD_FAILED reason=" + e.getMessage(), e);
        }
    }

    /**
     * Generates a signed RS256 JWT for the given user.
     *
     * @param userId the user's database ID
     * @param tenantId the tenant UUID this user belongs to
     * @param roleIds the set of role IDs assigned to the user
     * @return a compact serialized signed JWT string
     */
    public String generateToken(Long userId, UUID tenantId, Set<Long> roleIds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);

        JWTClaimsSet claims =
                new JWTClaimsSet.Builder()
                        .issuer(ISSUER)
                        .issueTime(Date.from(now))
                        .expirationTime(Date.from(expiresAt))
                        .claim(CLAIM_TENANT_ID, tenantId.toString())
                        .claim(CLAIM_USER_ID, userId)
                        .claim(CLAIM_ROLE_IDS, roleIds.stream().toList())
                        .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);

        try {
            JWSSigner signer = new RSASSASigner(privateKey);
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new IllegalStateException("event=JWT_SIGN_FAILED", e);
        }

        log.debug(
                "event=JWT_TOKEN_GENERATED userId={} tenantId={} expiresAt={}",
                userId,
                tenantId,
                expiresAt);
        return signedJWT.serialize();
    }

    /**
     * Validates a compact JWT string and returns its parsed claims.
     *
     * <p>Verification is stateless — only the RSA public key is used. No database access.
     *
     * @param token the compact serialized JWT
     * @return parsed {@link JWTClaimsSet}
     * @throws InvalidJwtException if the token is malformed, expired or has an invalid signature
     */
    public JWTClaimsSet validateAndExtractClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            if (!signedJWT.verify(verifier)) {
                throw new InvalidJwtException("JWT signature verification failed");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (claims.getExpirationTime() == null
                    || claims.getExpirationTime().before(new Date())) {
                throw new InvalidJwtException("JWT token has expired");
            }

            return claims;
        } catch (ParseException | JOSEException e) {
            throw new InvalidJwtException(
                    "JWT parsing or verification error: " + e.getMessage(), e);
        }
    }

    /** Extracts tenantId claim (String UUID) from pre-validated claims. */
    public String extractTenantId(JWTClaimsSet claims) {
        try {
            return claims.getStringClaim(CLAIM_TENANT_ID);
        } catch (ParseException e) {
            throw new InvalidJwtException("Cannot extract tenantId from JWT claims", e);
        }
    }

    /** Extracts userId claim (Long) from pre-validated claims. */
    public Long extractUserId(JWTClaimsSet claims) {
        try {
            Number userId = (Number) claims.getClaim(CLAIM_USER_ID);
            if (userId == null) {
                throw new InvalidJwtException("userId claim is missing from JWT");
            }
            return userId.longValue();
        } catch (ClassCastException e) {
            throw new InvalidJwtException("userId claim has unexpected type in JWT", e);
        }
    }

    /** Extracts roleIds claim (List<Long>) from pre-validated claims. */
    @SuppressWarnings("unchecked")
    public List<Long> extractRoleIds(JWTClaimsSet claims) {
        try {
            List<?> raw = claims.getListClaim(CLAIM_ROLE_IDS);
            if (raw == null) {
                return List.of();
            }
            return raw.stream().map(o -> ((Number) o).longValue()).toList();
        } catch (ParseException | ClassCastException e) {
            throw new InvalidJwtException("Cannot extract roleIds from JWT claims", e);
        }
    }

    // ── Key loading helpers ────────────────────────────────────────────────────

    private RSAPrivateKey loadPrivateKey(String resourcePath) throws IOException {
        try (InputStream is = resolveResource(resourcePath).getInputStream();
                PEMParser parser =
                        new PEMParser(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            Object parsed = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            // Soporte para formato antiguo PKCS#1 (BEGIN RSA PRIVATE KEY)
            if (parsed instanceof PEMKeyPair keyPair) {
                return (RSAPrivateKey) converter.getKeyPair(keyPair).getPrivate();
            }
            // Soporte para formato moderno PKCS#8 (BEGIN PRIVATE KEY) - ¡Este es el tuyo!
            else if (parsed instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo) {
                return (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
            }

            throw new IOException(
                    "Unsupported PEM object type for private key: " + parsed.getClass().getName());
        }
    }

    private RSAPublicKey loadPublicKey(String resourcePath) throws IOException {
        try (InputStream is = resolveResource(resourcePath).getInputStream();
                PEMParser parser =
                        new PEMParser(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            Object parsed = parser.readObject();
            if (parsed instanceof org.bouncycastle.asn1.x509.SubjectPublicKeyInfo spki) {
                return (RSAPublicKey) new JcaPEMKeyConverter().getPublicKey(spki);
            }
            throw new IOException(
                    "Unsupported PEM object type for public key: " + parsed.getClass().getName());
        }
    }

    private Resource resolveResource(String path) {
        ResourceLoader loader = new DefaultResourceLoader();
        return loader.getResource(path);
    }

    /** Unchecked exception raised for any JWT validation failure. */
    public static class InvalidJwtException extends RuntimeException {
        public InvalidJwtException(String message) {
            super(message);
        }

        public InvalidJwtException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
