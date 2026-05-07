package com.solveria.core.workforce.domain.model;

import com.solveria.core.shared.events.DomainEvent;
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
public class Position {

  private UUID positionId;
  private UUID unitId;
  private Job job;
  private PositionStatus status;
  private Boolean isBudgeted;
  private HeadcountPlan headcountPlan;

  @Builder.Default private transient List<DomainEvent> domainEvents = new ArrayList<>();

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
        .build();
  }

  public UUID getJobId() {
    return job != null ? job.getJobId() : null;
  }

  public void occupy() {
    if (!PositionStatus.VACANT.equals(status)) {
      throw new IllegalStateException("Solo se puede ocupar una posicion vacante");
    }
    headcountPlan.occupy();
    this.status = PositionStatus.OCCUPIED;
    addDomainEvent(new PositionAssignedEvent(positionId, unitId, Instant.now()));
  }

  public void vacate() {
    headcountPlan.vacate();
    if (headcountPlan.getCurrentSlots() == 0) {
      this.status = PositionStatus.VACANT;
      addDomainEvent(new PositionVacatedEvent(positionId, unitId, Instant.now()));
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

  public void addDomainEvent(DomainEvent event) {
    if (domainEvents == null) {
      domainEvents = new ArrayList<>();
    }
    domainEvents.add(event);
  }

  public List<DomainEvent> pullDomainEvents() {
    if (domainEvents == null || domainEvents.isEmpty()) {
      return List.of();
    }
    List<DomainEvent> events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }
}
