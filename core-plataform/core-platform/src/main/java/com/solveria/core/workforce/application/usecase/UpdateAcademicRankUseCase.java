package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.AcademicRank;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UpdateAcademicRankUseCase {

  private static final String RELATIONSHIP_NOT_FOUND = "RELATIONSHIP_NOT_FOUND";
  private static final String ACADEMIC_PROFILE_NOT_FOUND = "ACADEMIC_PROFILE_NOT_FOUND";

  private final RelationshipRepositoryPort relationshipRepositoryPort;

  public void execute(UUID relationshipId, UUID tenantId, String newRank) {
    Relationship relationship =
        relationshipRepositoryPort
            .findByRelationshipIdAndTenantId(relationshipId, tenantId)
            .orElseThrow(() -> new SolverException(RELATIONSHIP_NOT_FOUND));

    if (relationship.getAcademicProfile() == null) {
      throw new SolverException(ACADEMIC_PROFILE_NOT_FOUND);
    }

    AcademicRank rank = AcademicRank.valueOf(newRank.toUpperCase());
    relationship.getAcademicProfile().upgradeRank(rank);
    relationship.notifyAcademicProfileRankUpdated(rank.name());

    relationshipRepositoryPort.save(relationship);

    log.info(
        "event=CORE_WORKFORCE_ACADEMIC_RANK_UPDATE_SUCCESS targetId={}", relationshipId);
  }
}

