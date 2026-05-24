package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.CreateOrgUnitRequest;
import com.solveria.core.workforce.application.dto.OrgUnitResponse;
import com.solveria.core.workforce.application.port.OrgUnitRepositoryPort;
import com.solveria.core.workforce.domain.exception.OrgUnitNotFoundException;
import com.solveria.core.workforce.domain.model.OrgHierarchy;
import com.solveria.core.workforce.domain.model.OrgUnit;
import com.solveria.core.workforce.domain.model.vo.CostCenter;
import com.solveria.core.workforce.infrastructure.mapper.OrgUnitMapper;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrgUnitUseCase {

  private final OrgUnitRepositoryPort orgUnitRepositoryPort;
  private final OrgUnitMapper orgUnitMapper;

  @Transactional
  public OrgUnitResponse executeRoot(CreateOrgUnitRequest request) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    OrgUnit orgUnit = this.createAndSaveOrgUnit(tenantId, null, request);
    return orgUnitMapper.toResponse(orgUnit);
  }

  @Transactional
  public OrgUnitResponse executeChild(CreateOrgUnitRequest request, UUID parentId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    orgUnitRepositoryPort
        .findByUnitIdAndTenantId(parentId, tenantId)
        .orElseThrow(
            () ->
                new OrgUnitNotFoundException(
                    "OrgUnit padre no encontrado o no pertenece a este tenant"));

    OrgUnit orgUnit = this.createAndSaveOrgUnit(tenantId, parentId, request);
    return orgUnitMapper.toResponse(orgUnit);
  }

  private OrgUnit createAndSaveOrgUnit(UUID tenantId, UUID parentId, CreateOrgUnitRequest request) {
    CostCenter costCenter = CostCenter.create(request.getCostCode(), request.getCostDescription());
    OrgUnit.OrgUnitType unitType = OrgUnit.OrgUnitType.valueOf(request.getUnitType().toUpperCase());

    OrgUnit orgUnit =
        (parentId == null)
            ? OrgUnit.createRoot(tenantId, request.getName(), unitType, costCenter)
            : OrgUnit.createChild(tenantId, parentId, request.getName(), unitType, costCenter);

    if (parentId != null) {
      orgUnit.addHierarchy(
          OrgHierarchy.create(parentId, orgUnit.getUnitId(), "ADMINISTRATIVE", LocalDate.now()));
    }

    return orgUnitRepositoryPort.save(orgUnit);
  }
}
