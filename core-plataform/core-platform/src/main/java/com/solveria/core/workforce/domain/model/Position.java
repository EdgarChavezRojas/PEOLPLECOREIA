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
  private java.util.List<UUID> occupantPersonIds = new java.util.ArrayList<>();

  public Position() {}

  public Position(
      UUID positionId,
      UUID unitId,
      Job job,
      PositionStatus status,
      Boolean isBudgeted,
      HeadcountPlan headcountPlan,
      java.util.List<UUID> occupantPersonIds) {
    this.positionId = positionId;
    this.unitId = unitId;
    this.job = job;
    this.status = status;
    this.isBudgeted = isBudgeted;
    this.headcountPlan = headcountPlan;
    this.occupantPersonIds =
        occupantPersonIds != null ? occupantPersonIds : new java.util.ArrayList<>();
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
    if (headcountPlan != null && occupantPersonIds != null) {
      headcountPlan.setCurrentSlots(occupantPersonIds.size());
      if (PositionStatus.OCCUPIED.equals(status)
          && headcountPlan.getCurrentSlots().equals(headcountPlan.getMaxSlots())) {
        this.status = PositionStatus.FILLED;
      } else if (PositionStatus.FILLED.equals(status)
          && headcountPlan.getCurrentSlots() < headcountPlan.getMaxSlots()) {
        this.status = PositionStatus.OCCUPIED;
      } else if (headcountPlan.getCurrentSlots() == 0
          && (PositionStatus.OCCUPIED.equals(status) || PositionStatus.FILLED.equals(status))) {
        this.status = PositionStatus.VACANT;
      }
    }
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
    if (headcountPlan != null && occupantPersonIds != null) {
      headcountPlan.setCurrentSlots(occupantPersonIds.size());
    }
    return headcountPlan;
  }

  public void setHeadcountPlan(HeadcountPlan headcountPlan) {
    this.headcountPlan = headcountPlan;
  }

  public java.util.List<UUID> getOccupantPersonIds() {
    return occupantPersonIds;
  }

  public void setOccupantPersonIds(java.util.List<UUID> occupantPersonIds) {
    this.occupantPersonIds =
        occupantPersonIds != null ? occupantPersonIds : new java.util.ArrayList<>();
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
        new java.util.ArrayList<>());
  }

  public UUID getJobId() {
    return job != null ? job.getJobId() : null;
  }

  public void occupy(UUID personId) {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido para ocupar una posicion");
    }
    if (!hasVacancy()) {
      throw new IllegalStateException("No hay plazas vacantes disponibles en esta posicion");
    }
    if (occupantPersonIds.contains(personId)) {
      throw new IllegalStateException("La persona ya esta asignada a esta posicion");
    }
    headcountPlan.occupy();
    this.occupantPersonIds.add(personId);
    if (headcountPlan.getCurrentSlots().equals(headcountPlan.getMaxSlots())) {
      this.status = PositionStatus.FILLED;
    } else {
      this.status = PositionStatus.OCCUPIED;
    }
    registerEvent(new PositionAssignedEvent(positionId, unitId, Instant.now()));
  }

  public void vacate() {
    this.occupantPersonIds.clear();
    while (headcountPlan.getCurrentSlots() > 0) {
      headcountPlan.vacate();
    }
    this.status = PositionStatus.VACANT;
    registerEvent(new PositionVacatedEvent(positionId, unitId, Instant.now()));
  }

  public void vacate(UUID personId) {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido para liberar la posicion");
    }
    if (!occupantPersonIds.remove(personId)) {
      throw new IllegalArgumentException("La persona especificada no ocupa esta posicion");
    }
    headcountPlan.vacate();
    if (headcountPlan.getCurrentSlots() == 0) {
      this.status = PositionStatus.VACANT;
    } else {
      this.status = PositionStatus.OCCUPIED;
    }
    registerEvent(new PositionVacatedEvent(positionId, unitId, Instant.now()));
  }

  public void reserve() {
    if (!PositionStatus.VACANT.equals(status)) {
      throw new IllegalStateException("Solo se puede reservar una posicion vacante");
    }
    this.status = PositionStatus.RESERVED;
  }

  public boolean hasVacancy() {
    if (headcountPlan == null) {
      return false;
    }
    if (occupantPersonIds != null) {
      headcountPlan.setCurrentSlots(occupantPersonIds.size());
    }
    return headcountPlan.hasVacancy();
  }

  public void updateBudgeted(boolean budgeted) {
    this.isBudgeted = budgeted;
  }
}
