package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import com.solveria.core.workforce.domain.event.PositionAssignedEvent;
import com.solveria.core.workforce.domain.event.PositionVacatedEvent;
import com.solveria.core.workforce.domain.model.vo.HeadcountPlan;
import com.solveria.core.workforce.domain.model.vo.PositionStatus;
import java.time.Instant;
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
public class Position extends DomainRoot {

  private UUID positionId;
  private UUID unitId;
  private Job job;
  private PositionStatus status;
  private Boolean isBudgeted;
  private HeadcountPlan headcountPlan;
  private UUID personId;



  public static Position create(UUID unitId, UUID jobId, Boolean isBudgeted, Integer maxSlots) {
    if (unitId == null || jobId == null || maxSlots == null) {
      throw new IllegalArgumentException("unitId, jobId y maxSlots son requeridos");
    }
    if (maxSlots <= 0) {
      throw new IllegalArgumentException("maxSlots debe ser mayor a 0");
    }

    return Position.builder()
        .positionId(UUID.randomUUID())
        .unitId(unitId)
        .job(Job.builder().jobId(jobId).build())
        .status(PositionStatus.VACANT)
        .isBudgeted(isBudgeted != null ? isBudgeted : false)
        .headcountPlan(HeadcountPlan.create(maxSlots))
        .personId(null)
        .build();
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
