package com.solveria.core.security.filter;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SecurityContextFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(SecurityContextFilter.class);
  private static final String BEARER_PREFIX = "Bearer ";

  @FunctionalInterface
  public interface JwtClaimsExtractor {
    Claims extract(String token);
  }

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

        Claims claims = claimsExtractor.extract(token);

        // 🔴 VALIDACIÓN OBLIGATORIA
        if (claims.userId() == null) {
          throw new RuntimeException("JWT missing userId");
        }

        if (claims.tenantId() != null && !claims.tenantId().isBlank()) {
          SecurityTenantContext.setTenantId(claims.tenantId());
        }

        SecurityUserContext.setUserId(claims.userId());

        // Marca la petición como autenticada para Spring Security
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(claims.userId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug(
            "event=SECURITY_CONTEXT_SET tenantId={} userId={}", claims.tenantId(), claims.userId());
      }

      filterChain.doFilter(request, response);

    } catch (RuntimeException ex) {

      // 🔴 IMPORTANTE: no dejar pasar requests inválidos
      log.warn(
          "event=SECURITY_CONTEXT_INVALID path={} reason={}",
          request.getRequestURI(),
          ex.getMessage());

      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response
          .getWriter()
          .write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Invalid or missing JWT\"}");

    } finally {
      SecurityTenantContext.clear();
      SecurityUserContext.clear();
      SecurityContextHolder.clearContext();
    }
  }
}
