package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.EmploymentCondition;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpdateEmploymentConditionsUseCase {

  private static final String RELATIONSHIP_NOT_FOUND = "RELATIONSHIP_NOT_FOUND";

  private final RelationshipRepositoryPort relationshipRepositoryPort;

  public void execute(UUID relationshipId, EmploymentCondition condition) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    Relationship relationship =
        relationshipRepositoryPort
            .findByRelationshipIdAndTenantId(relationshipId, tenantId)
            .orElseThrow(() -> new SolverException(RELATIONSHIP_NOT_FOUND));

    relationship.updateEmploymentCondition(condition);
    relationshipRepositoryPort.save(relationship);

    log.info(
        "event=CORE_WORKFORCE_EMPLOYMENT_CONDITION_UPDATE_SUCCESS targetId={}", relationshipId);
  }
}

