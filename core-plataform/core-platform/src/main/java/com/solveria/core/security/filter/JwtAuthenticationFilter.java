package com.solveria.core.security.filter;

import com.solveria.core.security.context.SecurityUserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {

      // 🔴 TEMPORAL (hasta que leas JWT real)
      String userIdHeader = request.getHeader("X-USER-ID");

      if (userIdHeader != null) {
        SecurityUserContext.setUserId(Long.parseLong(userIdHeader));
      }

      filterChain.doFilter(request, response);

    } finally {
      SecurityUserContext.clear();
    }
  }
}
