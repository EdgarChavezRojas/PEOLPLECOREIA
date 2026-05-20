package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.StatusLog;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReactivateRelationshipUseCase {

  private static final String RELATIONSHIP_NOT_FOUND = "RELATIONSHIP_NOT_FOUND";

  private final RelationshipRepositoryPort relationshipRepositoryPort;

  public void execute(UUID relationshipId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    Relationship relationship =
        relationshipRepositoryPort
            .findByRelationshipIdAndTenantId(relationshipId, tenantId)
            .orElseThrow(() -> new SolverException(RELATIONSHIP_NOT_FOUND));

    RelationshipStatus previousStatus = relationship.getCurrentStatus();
    relationship.reactivate();
    relationship.addStatusLog(
        StatusLog.create(relationshipId, previousStatus, RelationshipStatus.ACTIVE, null, null));

    relationshipRepositoryPort.save(relationship);

    log.info("event=CORE_WORKFORCE_RELATIONSHIP_REACTIVATE_SUCCESS targetId={}", relationshipId);
  }
}
