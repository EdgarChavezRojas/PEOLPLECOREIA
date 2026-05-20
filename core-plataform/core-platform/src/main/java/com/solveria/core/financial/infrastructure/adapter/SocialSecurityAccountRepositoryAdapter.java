package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.port.SocialSecurityAccountRepositoryPort;
import com.solveria.core.financial.domain.model.SocialSecurityAccount;
import com.solveria.core.financial.infrastructure.mapper.SocialSecurityAccountMapper;
import com.solveria.core.financial.infrastructure.repository.SocialSecurityAccountRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter: SocialSecurityAccountRepositoryPort. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SocialSecurityAccountRepositoryAdapter implements SocialSecurityAccountRepositoryPort {

  private final SocialSecurityAccountRepository ssaRepository;
  private final SocialSecurityAccountMapper ssaMapper;

  @Override
  @Transactional
  public void save(SocialSecurityAccount account) {
    ssaRepository.save(ssaMapper.toJpa(account));
  }

  @Override
  public Optional<SocialSecurityAccount> findById(UUID ssaId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return ssaRepository.findBySsaIdAndTenantId(ssaId, currentTenantId).map(ssaMapper::toDomain);
  }

  @Override
  public Optional<SocialSecurityAccount> findByPersonId(UUID personId, UUID tenantId) {
    return ssaRepository.findByPersonIdAndTenantId(personId, tenantId).map(ssaMapper::toDomain);
  }
}
