package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.OrgUnitRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.OrgUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoveOrgUnitUseCase {

  private static final String ORG_UNIT_NOT_FOUND = "ORG_UNIT_NOT_FOUND";

  private final OrgUnitRepositoryPort orgUnitRepositoryPort;

  public void execute(UUID unitId, UUID newParentId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    OrgUnit orgUnit =
        orgUnitRepositoryPort
            .findByUnitIdAndTenantId(unitId, tenantId)
            .orElseThrow(() -> new SolverException(ORG_UNIT_NOT_FOUND));

    if (newParentId != null) {
      orgUnitRepositoryPort
          .findByUnitIdAndTenantId(newParentId, tenantId)
          .orElseThrow(() -> new SolverException(ORG_UNIT_NOT_FOUND));
    }

    orgUnit.changeAssignment(newParentId);
    orgUnitRepositoryPort.save(orgUnit);

    log.info("event=CORE_WORKFORCE_ORGUNIT_MOVE_SUCCESS targetId={}", unitId);
  }
}

