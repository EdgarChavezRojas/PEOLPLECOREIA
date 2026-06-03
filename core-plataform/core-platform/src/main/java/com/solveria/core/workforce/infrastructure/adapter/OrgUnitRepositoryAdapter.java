package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import com.solveria.core.shared.pagination.PageUtils;
import com.solveria.core.workforce.application.port.OrgUnitRepositoryPort;
import com.solveria.core.workforce.domain.model.OrgUnit;
import com.solveria.core.workforce.domain.model.OrgUnit.OrgUnitType;
import com.solveria.core.workforce.infrastructure.jpa.OrgUnitJpa;
import com.solveria.core.workforce.infrastructure.mapper.OrgUnitMapper;
import com.solveria.core.workforce.infrastructure.repository.OrgUnitRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    OrgUnitJpa orgUnitJpa =
        orgUnitRepository
            .findById(orgUnit.getUnitId())
            .map(
                existing -> {
                  orgUnitMapper.updateJpa(orgUnit, existing);
                  return existing;
                })
            .orElseGet(() -> orgUnitMapper.toJpa(orgUnit));

    OrgUnitJpa savedOrgUnitJpa = orgUnitRepository.save(orgUnitJpa);
    OrgUnit savedOrgUnit = orgUnitMapper.toDomain(savedOrgUnitJpa);

    eventOutboxPort.publish(orgUnit.pullDomainEvents());

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

  @Override
  public Page<OrgUnit> findByTenantId(UUID tenantId, Pageable pageable) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return Page.empty(pageable);
    }
    List<OrgUnit> orgUnits =
        orgUnitRepository.findByTenantId(currentTenantId).stream()
            .map(orgUnitMapper::toDomain)
            .toList();
    return PageUtils.slice(orgUnits, pageable);
  }

  @Override
  public boolean existsByNameAndUnitTypeAndTenantId(
      String name, OrgUnitType unitType, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return false;
    }
    return orgUnitRepository.existsByNameAndUnitTypeAndTenantId(name, unitType, currentTenantId);
  }
}
