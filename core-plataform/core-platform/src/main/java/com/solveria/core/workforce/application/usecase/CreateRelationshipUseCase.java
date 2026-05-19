package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.application.dto.CreateRelationshipRequest;
import com.solveria.core.workforce.application.dto.RelationshipResponse;
import com.solveria.core.workforce.domain.exception.PersonNotFoundException;
import com.solveria.core.workforce.domain.model.AcademicProfile;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.StatusLog;
import com.solveria.core.workforce.domain.model.WorkerProfile;
import com.solveria.core.workforce.domain.model.vo.AcademicRank;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import com.solveria.core.workforce.infrastructure.mapper.RelationshipMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateRelationshipUseCase {

  private final RelationshipRepositoryPort relationshipRepositoryPort;
  private final PersonRepositoryPort personRepositoryPort;
  private final RelationshipMapper relationshipMapper;

  @Transactional
  public RelationshipResponse execute(CreateRelationshipRequest request) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    if (!tenantId.equals(request.tenantId())) {
      throw new IllegalStateException("Tenant inconsistente entre request y contexto de seguridad");
    }

    personRepositoryPort
        .findByPersonId(request.personId())
        .orElseThrow(() -> new PersonNotFoundException("Person no encontrado"));

    RelationshipType relType = RelationshipType.valueOf(request.relationType().toUpperCase());

    if (RelationshipType.LABOR.equals(relType)
        && relationshipRepositoryPort.existsPrimaryRelationshipForPersonInTenant(
            request.personId(), tenantId)) {
      throw new IllegalStateException(
          "Invariante violada: un colaborador no puede tener dos vinculos primarios activos en el tenant");
    }

    Relationship relationship =
        Relationship.create(request.personId(), tenantId, relType, request.hireDate());

    if (RelationshipType.ACADEMIC.equals(relType)) {
      relationship.assignAcademicProfile(
          AcademicProfile.create(
              relationship.getRelationshipId(),
              AcademicRank.ASSITANT,
              request.teachingLoad() != null ? request.teachingLoad() : 20));
    } else {
      relationship.assignWorkerProfile(
          WorkerProfile.create(
              relationship.getRelationshipId(),
              request.employeeNo(),
              request.department(),
              request.jobTitle()));
    }

    relationship.addStatusLog(
        StatusLog.create(
            relationship.getRelationshipId(),
            null,
            RelationshipStatus.DRAFT,
            "Relacion creada",
            null));

    Relationship savedRelationship = relationshipRepositoryPort.save(relationship);

    log.info(
        "event=RELATIONSHIP_CREATE_SUCCESS relationshipId={} personId={}",
        savedRelationship.getRelationshipId(),
        savedRelationship.getPersonId());

    return relationshipMapper.toResponse(savedRelationship);
  }
}
