package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.OrgUnitRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.OrgUnit;
import com.solveria.core.workforce.domain.model.vo.Extension;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RelocateOrgUnitUseCase {

  private static final String ORG_UNIT_NOT_FOUND = "ORG_UNIT_NOT_FOUND";
  private static final String ORG_UNIT_GEO_EXTENSION_REQUIRED = "ORG_UNIT_GEO_EXTENSION_REQUIRED";

  private final OrgUnitRepositoryPort orgUnitRepositoryPort;

  @Transactional
  public void execute(UUID unitId, Extension geoExtension) {
    if (geoExtension == null) {
      throw new SolverException(ORG_UNIT_GEO_EXTENSION_REQUIRED);
    }

    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    OrgUnit orgUnit =
        orgUnitRepositoryPort
            .findByUnitIdAndTenantId(unitId, tenantId)
            .orElseThrow(() -> new SolverException(ORG_UNIT_NOT_FOUND));

    orgUnit.updateGeoCoords(geoExtension.name());
    orgUnitRepositoryPort.save(orgUnit);

    log.info("event=CORE_WORKFORCE_ORGUNIT_RELOCATE_SUCCESS targetId={}", unitId);
  }
}
