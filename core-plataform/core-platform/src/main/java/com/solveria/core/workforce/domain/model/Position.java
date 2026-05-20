package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.event.PositionAssignedEvent;
import com.solveria.core.workforce.domain.event.PositionVacatedEvent;
import com.solveria.core.workforce.domain.model.vo.HeadcountPlan;
import com.solveria.core.workforce.domain.model.vo.PositionStatus;
import java.time.Instant;
import java.util.UUID;

public class Position extends DomainRoot {

  private UUID positionId;
  private UUID unitId;
  private Job job;
  private PositionStatus status;
  private Boolean isBudgeted;
  private HeadcountPlan headcountPlan;
  private UUID personId;

  public Position() {}

  public Position(
      UUID positionId,
      UUID unitId,
      Job job,
      PositionStatus status,
      Boolean isBudgeted,
      HeadcountPlan headcountPlan,
      UUID personId) {
    this.positionId = positionId;
    this.unitId = unitId;
    this.job = job;
    this.status = status;
    this.isBudgeted = isBudgeted;
    this.headcountPlan = headcountPlan;
    this.personId = personId;
  }

  public UUID getPositionId() {
    return positionId;
  }

  public void setPositionId(UUID positionId) {
    this.positionId = positionId;
  }

  public UUID getUnitId() {
    return unitId;
  }

  public void setUnitId(UUID unitId) {
    this.unitId = unitId;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public PositionStatus getStatus() {
    return status;
  }

  public void setStatus(PositionStatus status) {
    this.status = status;
  }

  public Boolean getIsBudgeted() {
    return isBudgeted;
  }

  public void setIsBudgeted(Boolean isBudgeted) {
    this.isBudgeted = isBudgeted;
  }

  public HeadcountPlan getHeadcountPlan() {
    return headcountPlan;
  }

  public void setHeadcountPlan(HeadcountPlan headcountPlan) {
    this.headcountPlan = headcountPlan;
  }

  public UUID getPersonId() {
    return personId;
  }

  public void setPersonId(UUID personId) {
    this.personId = personId;
  }

  public static Position create(UUID unitId, UUID jobId, Boolean isBudgeted, Integer maxSlots) {
    if (unitId == null || jobId == null || maxSlots == null) {
      throw new IllegalArgumentException("unitId, jobId y maxSlots son requeridos");
    }
    if (maxSlots <= 0) {
      throw new IllegalArgumentException("maxSlots debe ser mayor a 0");
    }
    Job associatedJob = new Job();
    associatedJob.setJobId(jobId);

    return new Position(
        UUID.randomUUID(),
        unitId,
        associatedJob,
        PositionStatus.VACANT,
        isBudgeted != null ? isBudgeted : false,
        HeadcountPlan.create(maxSlots),
        null);
  }

  public UUID getJobId() {
    return job != null ? job.getJobId() : null;
  }

  public void occupy(UUID personId) {
    if (!PositionStatus.VACANT.equals(status)) {
      throw new IllegalStateException("Solo se puede ocupar una posicion vacante");
    }
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido para ocupar una posicion");
    }
    headcountPlan.occupy();
    this.status = PositionStatus.OCCUPIED;
    this.personId = personId;
    registerEvent(new PositionAssignedEvent(positionId, unitId, Instant.now()));
  }

  public void vacate() {
    headcountPlan.vacate();
    if (headcountPlan.getCurrentSlots() == 0) {
      this.status = PositionStatus.VACANT;
      this.personId = null;
      registerEvent(new PositionVacatedEvent(positionId, unitId, Instant.now()));
    }
  }

  public void reserve() {
    if (!PositionStatus.VACANT.equals(status)) {
      throw new IllegalStateException("Solo se puede reservar una posicion vacante");
    }
    this.status = PositionStatus.RESERVED;
  }

  public boolean hasVacancy() {
    return headcountPlan.hasVacancy();
  }

  public void updateBudgeted(boolean budgeted) {
    this.isBudgeted = budgeted;
  }
}
