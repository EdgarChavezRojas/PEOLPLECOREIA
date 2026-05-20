package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.port.HealthProviderRepositoryPort;
import com.solveria.core.financial.domain.model.HealthProvider;
import com.solveria.core.financial.infrastructure.mapper.HealthProviderMapper;
import com.solveria.core.financial.infrastructure.repository.HealthProviderRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter: HealthProviderRepositoryPort. */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthProviderRepositoryAdapter implements HealthProviderRepositoryPort {

  private final HealthProviderRepository healthProviderRepository;
  private final HealthProviderMapper healthProviderMapper;

  @Override
  @Transactional
  public void save(HealthProvider provider) {
    healthProviderRepository.save(healthProviderMapper.toJpa(provider));
  }

  @Override
  public Optional<HealthProvider> findById(UUID providerId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return healthProviderRepository
        .findByProviderIdAndTenantId(providerId, currentTenantId)
        .map(healthProviderMapper::toDomain);
  }
}
