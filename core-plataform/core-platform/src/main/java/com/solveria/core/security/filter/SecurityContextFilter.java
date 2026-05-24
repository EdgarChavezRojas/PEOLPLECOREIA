package com.solveria.core.security.filter;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security filter that extracts tenant and user identity from a validated JWT bearer token.
 *
 * <p>The filter is intentionally decoupled from the concrete {@code JwtService} via a functional
 * interface {@link JwtClaimsExtractor}, so that {@code core-platform} stays free of any direct
 * dependency on JOSE/Nimbus libraries (which live in {@code iam-service}).
 *
 * <p>The concrete extractor is supplied by {@code iam-service} at wiring time (see
 * {@code SecurityBeansConfig}).
 *
 * <p>Request flow:
 * <ol>
 *   <li>Reads {@code Authorization: Bearer <token>} header.
 *   <li>Delegates token validation and claim extraction to {@link JwtClaimsExtractor}.
 *   <li>Injects {@code tenantId} and {@code userId} into the thread-local security contexts.
 *   <li>Always clears the contexts in the {@code finally} block.
 * </ol>
 *
 * <!-- TODO: Si en el futuro se implementa un API Gateway, revertir a lectura de headers
 *      X-Tenant-Id y X-User-Id inyectados por el Gateway -->
 */
public class SecurityContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityContextFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Functional interface that decouples this filter from the concrete JWT library.
     *
     * <p>The implementation is provided by {@code iam-service} during bean wiring.
     */
    @FunctionalInterface
    public interface JwtClaimsExtractor {
        /**
         * Validates the token and returns an opaque claims holder.
         *
         * @param token the raw JWT string (without the "Bearer " prefix)
         * @return a {@link Claims} record containing tenantId and userId
         * @throws RuntimeException (e.g. InvalidJwtException) if the token is invalid
         */
        Claims extract(String token);
    }

    /** Typed result of JWT validation. */
    public record Claims(String tenantId, Long userId) {}

    private final JwtClaimsExtractor claimsExtractor;

    public SecurityContextFilter(JwtClaimsExtractor claimsExtractor) {
        this.claimsExtractor = claimsExtractor;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());

                try {
                    Claims claims = claimsExtractor.extract(token);

                    if (claims.tenantId() != null && !claims.tenantId().isBlank()) {
                        SecurityTenantContext.setTenantId(claims.tenantId());
                    }

                    if (claims.userId() != null) {
                        SecurityUserContext.setUserId(claims.userId());
                    }

                    log.debug(
                            "event=SECURITY_CONTEXT_SET tenantId={} userId={}",
                            claims.tenantId(),
                            claims.userId());

                } catch (RuntimeException ex) {
                    // Invalid token: log and continue without populating context.
                    // Spring Security's authenticationEntryPoint will handle 401 for
                    // protected endpoints.
                    log.warn(
                            "event=SECURITY_CONTEXT_JWT_INVALID path={} reason={}",
                            request.getRequestURI(),
                            ex.getMessage());
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            SecurityTenantContext.clear();
            SecurityUserContext.clear();
        }
    }
}

