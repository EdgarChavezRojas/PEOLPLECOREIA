package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.UpdateRelationshipStatusRequest;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.StatusLog;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateRelationshipStatusUseCase {

  private static final String RELATIONSHIP_NOT_FOUND = "RELATIONSHIP_NOT_FOUND";
  private static final String RELATIONSHIP_STATUS_NOT_ALLOWED = "RELATIONSHIP_STATUS_NOT_ALLOWED";
  private static final String RELATIONSHIP_STATUS_REQUIRED = "RELATIONSHIP_STATUS_REQUIRED";

  private final RelationshipRepositoryPort relationshipRepositoryPort;

  @Transactional
  public void execute(UUID relationshipId, UpdateRelationshipStatusRequest request) {
    if (request == null || request.getStatus() == null) {
      throw new SolverException(RELATIONSHIP_STATUS_REQUIRED);
    }

    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    Relationship relationship =
        relationshipRepositoryPort
            .findByRelationshipIdAndTenantId(relationshipId, tenantId)
            .orElseThrow(() -> new SolverException(RELATIONSHIP_NOT_FOUND));

    RelationshipStatus previousStatus = relationship.getCurrentStatus();
    RelationshipStatus targetStatus = request.getStatus();

    switch (targetStatus) {
      case ACTIVE -> {
        relationship.activate();
        relationship.addStatusLog(
            StatusLog.create(
                relationshipId,
                previousStatus,
                RelationshipStatus.ACTIVE,
                "RELATIONSHIP_ACTIVATED",
                null));
      }
      case SUSPENDED -> {
        relationship.suspend();
        relationship.addStatusLog(
            StatusLog.create(
                relationshipId,
                previousStatus,
                RelationshipStatus.SUSPENDED,
                "RELATIONSHIP_SUSPENDED",
                null));
      }
      default -> throw new SolverException(RELATIONSHIP_STATUS_NOT_ALLOWED);
    }

    relationshipRepositoryPort.save(relationship);

    log.info(
        "event=CORE_WORKFORCE_RELATIONSHIP_STATUS_UPDATE_SUCCESS targetId={} status={}",
        relationshipId,
        targetStatus);
  }
}
