package com.solveria.core.shared.config;

import com.solveria.core.security.context.SecurityUserContext;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

  @Bean
  public AuditorAware<String> auditorAware() {
    // getUserIdentifier() retorna el userId del JWT como String,
    // o "system" como fallback si no hay usuario autenticado en el hilo actual.
    return () -> Optional.of(SecurityUserContext.getUserIdentifier());
  }
}
