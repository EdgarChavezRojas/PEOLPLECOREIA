package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.EventOutboxPort;
import com.solveria.core.workforce.application.port.OrgUnitRepositoryPort;
import com.solveria.core.workforce.domain.model.OrgUnit;
import com.solveria.core.workforce.infrastructure.jpa.OrgUnitJpa;
import com.solveria.core.workforce.infrastructure.mapper.OrgUnitMapper;
import com.solveria.core.workforce.infrastructure.repository.OrgUnitRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrgUnitRepositoryAdapter implements OrgUnitRepositoryPort {

  private final OrgUnitRepository orgUnitRepository;
  private final OrgUnitMapper orgUnitMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public OrgUnit save(OrgUnit orgUnit) {
    OrgUnitJpa orgUnitJpa = orgUnitMapper.toJpa(orgUnit);
    OrgUnitJpa savedOrgUnitJpa = orgUnitRepository.save(orgUnitJpa);
    OrgUnit savedOrgUnit = orgUnitMapper.toDomain(savedOrgUnitJpa);

    for (DomainEvent event : orgUnit.pullDomainEvents()) {
      eventOutboxPort.publish(event);
    }

    return savedOrgUnit;
  }

  @Override
  public Optional<OrgUnit> findByUnitIdAndTenantId(UUID unitId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return Optional.empty();
    }
    return orgUnitRepository
        .findByUnitIdAndTenantId(unitId, currentTenantId)
        .map(orgUnitMapper::toDomain);
  }

  @Override
  public boolean existsByUnitIdAndTenantId(UUID unitId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return false;
    }
    return orgUnitRepository.findByUnitIdAndTenantId(unitId, currentTenantId).isPresent();
  }
}
