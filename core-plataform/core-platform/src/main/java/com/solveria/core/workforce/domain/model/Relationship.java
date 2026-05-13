package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.event.AcademicProfileRankUpdatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipCreatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipEndedEvent;
import com.solveria.core.workforce.domain.event.RelationshipReactivatedEvent;
import com.solveria.core.workforce.domain.model.vo.EmploymentCondition;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Relationship extends DomainRoot {

  private UUID relationshipId;
  private UUID personId;
  private UUID tenantId;
  private RelationshipType relationType;
  private RelationshipStatus currentStatus;
  private LocalDate hireDate;
  private LocalDate createdAt;
  private LocalDate updatedAt;

  private WorkerProfile workerProfile;
  private AcademicProfile academicProfile;
  private EmploymentCondition employmentCondition;

  @Builder.Default private List<StatusLog> statusLogs = new ArrayList<>();



  public static Relationship create(
      UUID personId, UUID tenantId, RelationshipType relationType, LocalDate hireDate) {
    if (personId == null || tenantId == null || relationType == null) {
      throw new IllegalArgumentException("personId, tenantId y relationType son requeridos");
    }

    Relationship relationship =
        Relationship.builder()
            .relationshipId(UUID.randomUUID())
            .personId(personId)
            .tenantId(tenantId)
            .relationType(relationType)
            .currentStatus(RelationshipStatus.DRAFT)
            .hireDate(hireDate)
            .createdAt(LocalDate.now())
            .updatedAt(LocalDate.now())
            .statusLogs(new ArrayList<>())
            .build();
    relationship.registerEvent(
        new RelationshipCreatedEvent(
            relationship.relationshipId, relationship.personId, relationship.tenantId, Instant.now()));
    return relationship;
  }

  public void assignWorkerProfile(WorkerProfile profile) {
    this.workerProfile = profile;
    this.academicProfile = null;
    this.updatedAt = LocalDate.now();
  }

  public void assignAcademicProfile(AcademicProfile profile) {
    this.academicProfile = profile;
    this.workerProfile = null;
    this.updatedAt = LocalDate.now();
  }

  public void addStatusLog(StatusLog statusLog) {
    if (statusLog == null) {
      throw new IllegalArgumentException("statusLog no puede ser nulo");
    }
    if (statusLogs == null) {
      statusLogs = new ArrayList<>();
    }
    statusLogs.add(statusLog);
  }

  public void activate() {
    if (!RelationshipStatus.DRAFT.equals(currentStatus)) {
      throw new IllegalStateException("Solo se puede activar una relacion en estado DRAFT");
    }
    this.currentStatus = RelationshipStatus.ACTIVE;
    this.updatedAt = LocalDate.now();
  }

  public void suspend() {
    this.currentStatus = RelationshipStatus.SUSPENDED;
    this.updatedAt = LocalDate.now();
  }

  public void reactivate() {
    if (!RelationshipStatus.SUSPENDED.equals(currentStatus)) {
      throw new IllegalStateException("Solo se puede reactivar una relacion SUSPENDIDA");
    }
    this.currentStatus = RelationshipStatus.ACTIVE;
    this.updatedAt = LocalDate.now();
    registerEvent(new RelationshipReactivatedEvent(relationshipId, Instant.now()));
  }

  public void terminate() {
    this.currentStatus = RelationshipStatus.TERMINATED;
    this.updatedAt = LocalDate.now();
    registerEvent(new RelationshipEndedEvent(relationshipId, Instant.now()));
  }

  public void updateEmploymentCondition(EmploymentCondition condition) {
    if (condition == null) {
      throw new SolverException("EMPLOYMENT_CONDITION_REQUIRED");
    }
    this.employmentCondition = condition;
    this.updatedAt = LocalDate.now();
    addStatusLog(
        StatusLog.create(
            relationshipId,
            currentStatus,
            currentStatus,
            "EMPLOYMENT_CONDITION_UPDATED",
            null));
  }

  public void notifyAcademicProfileRankUpdated(String newRank) {
    registerEvent(new AcademicProfileRankUpdatedEvent(relationshipId, newRank, Instant.now()));
    addStatusLog(
        StatusLog.create(
            relationshipId,
            currentStatus,
            currentStatus,
            "ACADEMIC_RANK_UPDATED",
            null));
  }


}
