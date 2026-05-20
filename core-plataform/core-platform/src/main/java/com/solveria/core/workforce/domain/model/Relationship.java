package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.event.AcademicProfileRankUpdatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipCreatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipEndedEvent;
import com.solveria.core.workforce.domain.event.RelationshipReactivatedEvent;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.vo.EmploymentCondition;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

  private List<StatusLog> statusLogs;

  public Relationship() {
    this.statusLogs = new ArrayList<>();
  }

  public Relationship(
      UUID relationshipId,
      UUID personId,
      UUID tenantId,
      RelationshipType relationType,
      RelationshipStatus currentStatus,
      LocalDate hireDate,
      LocalDate createdAt,
      LocalDate updatedAt,
      WorkerProfile workerProfile,
      AcademicProfile academicProfile,
      EmploymentCondition employmentCondition,
      List<StatusLog> statusLogs) {
    this.relationshipId = relationshipId;
    this.personId = personId;
    this.tenantId = tenantId;
    this.relationType = relationType;
    this.currentStatus = currentStatus;
    this.hireDate = hireDate;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.workerProfile = workerProfile;
    this.academicProfile = academicProfile;
    this.employmentCondition = employmentCondition;
    this.statusLogs = statusLogs != null ? statusLogs : new ArrayList<>();
  }

  // Getters y Setters...
  public UUID getRelationshipId() {
    return relationshipId;
  }

  public void setRelationshipId(UUID relationshipId) {
    this.relationshipId = relationshipId;
  }

  public UUID getPersonId() {
    return personId;
  }

  public void setPersonId(UUID personId) {
    this.personId = personId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public RelationshipType getRelationType() {
    return relationType;
  }

  public void setRelationType(RelationshipType relationType) {
    this.relationType = relationType;
  }

  public RelationshipStatus getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(RelationshipStatus currentStatus) {
    this.currentStatus = currentStatus;
  }

  public LocalDate getHireDate() {
    return hireDate;
  }

  public void setHireDate(LocalDate hireDate) {
    this.hireDate = hireDate;
  }

  public LocalDate getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDate createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDate getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDate updatedAt) {
    this.updatedAt = updatedAt;
  }

  public WorkerProfile getWorkerProfile() {
    return workerProfile;
  }

  public void setWorkerProfile(WorkerProfile workerProfile) {
    this.workerProfile = workerProfile;
  }

  public AcademicProfile getAcademicProfile() {
    return academicProfile;
  }

  public void setAcademicProfile(AcademicProfile academicProfile) {
    this.academicProfile = academicProfile;
  }

  public EmploymentCondition getEmploymentCondition() {
    return employmentCondition;
  }

  public void setEmploymentCondition(EmploymentCondition employmentCondition) {
    this.employmentCondition = employmentCondition;
  }

  public List<StatusLog> getStatusLogs() {
    return statusLogs;
  }

  public void setStatusLogs(List<StatusLog> statusLogs) {
    this.statusLogs = statusLogs != null ? statusLogs : new ArrayList<>();
  }

  public static Relationship create(
      UUID personId, UUID tenantId, RelationshipType relationType, LocalDate hireDate) {
    if (personId == null || tenantId == null || relationType == null) {
      throw new IllegalArgumentException("personId, tenantId y relationType son requeridos");
    }

    Relationship relationship =
        new Relationship(
            UUID.randomUUID(),
            personId,
            tenantId,
            relationType,
            RelationshipStatus.DRAFT,
            hireDate,
            LocalDate.now(),
            LocalDate.now(),
            null,
            null,
            null,
            new ArrayList<>());

    relationship.registerEvent(
        new RelationshipCreatedEvent(
            relationship.getRelationshipId(),
            relationship.getPersonId(),
            relationship.getTenantId(),
            Instant.now()));
    return relationship;
  }

  // Resto de los métodos de negocio (assignWorkerProfile, addStatusLog, activate, etc.) sin
  // cambios...
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
    if (RelationshipStatus.ACTIVE.equals(this.currentStatus)) return;
    if (!RelationshipStatus.DRAFT.equals(this.currentStatus)) {
      throw new IllegalStateException(
          "Solo se puede activar una relación en estado DRAFT. Estado actual: "
              + this.currentStatus);
    }
    this.currentStatus = RelationshipStatus.ACTIVE;
    this.updatedAt = LocalDate.now();
  }

  public void suspend() {
    if (RelationshipStatus.SUSPENDED.equals(this.currentStatus)) return;
    if (!RelationshipStatus.ACTIVE.equals(this.currentStatus)) {
      throw new IllegalStateException(
          "Solo se puede suspender una relación en estado ACTIVE. Estado actual: "
              + this.currentStatus);
    }
    this.currentStatus = RelationshipStatus.SUSPENDED;
    this.updatedAt = LocalDate.now();
  }

  public void reactivate() {
    if (RelationshipStatus.ACTIVE.equals(this.currentStatus)) return;
    if (!RelationshipStatus.SUSPENDED.equals(this.currentStatus)) {
      throw new IllegalStateException(
          "Solo se puede reactivar una relación en estado SUSPENDED. Estado actual: "
              + this.currentStatus);
    }
    this.currentStatus = RelationshipStatus.ACTIVE;
    this.updatedAt = LocalDate.now();
    registerEvent(new RelationshipReactivatedEvent(this.relationshipId, Instant.now()));
  }

  public void terminate() {
    if (RelationshipStatus.TERMINATED.equals(this.currentStatus)) return;
    if (RelationshipStatus.DRAFT.equals(this.currentStatus)) {
      throw new IllegalStateException(
          "No se puede terminar una relación en estado DRAFT, debe ser cancelada.");
    }
    this.currentStatus = RelationshipStatus.TERMINATED;
    this.updatedAt = LocalDate.now();
    registerEvent(new RelationshipEndedEvent(this.relationshipId, this.tenantId));
  }

  public void updateEmploymentCondition(EmploymentCondition condition) {
    if (condition == null) {
      throw new SolverException("EMPLOYMENT_CONDITION_REQUIRED");
    }
    this.employmentCondition = condition;
    this.updatedAt = LocalDate.now();
    addStatusLog(
        StatusLog.create(
            relationshipId, currentStatus, currentStatus, "EMPLOYMENT_CONDITION_UPDATED", null));
  }

  public void notifyAcademicProfileRankUpdated(String newRank) {
    registerEvent(new AcademicProfileRankUpdatedEvent(relationshipId, newRank, Instant.now()));
    addStatusLog(
        StatusLog.create(
            relationshipId, currentStatus, currentStatus, "ACADEMIC_RANK_UPDATED", null));
  }
}
