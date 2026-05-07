package com.solveria.core.financial.application.port;

import com.solveria.core.financial.domain.model.HealthProvider;
import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Repositorio de HealthProvider. */
public interface HealthProviderRepositoryPort {

  Optional<HealthProvider> findById(UUID providerId);

  void save(HealthProvider provider);
}
