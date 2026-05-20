package com.solveria.scheduling.domain.model.ar;

import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.entity.AssignedShift;
import com.solveria.scheduling.domain.model.enums.PlanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

/**
 * Root Entity para la gestión de la planificación de horarios (Roster Management). Contiene la
 * malla de turnos (AssignedShift).
 */
@Getter
public class SchedulePlan {

  private final UUID planId;
  private final UUID unitId;
  private final LocalDate periodStart;
  private final LocalDate periodEnd;
  private PlanStatus status;
  private BigDecimal totalProjectedCost;
  private final List<AssignedShift> shifts = new ArrayList<>();

  public SchedulePlan(UUID unitId, LocalDate periodStart, LocalDate periodEnd) {
    this.planId = UUID.randomUUID();
    this.unitId = unitId;
    this.periodStart = periodStart;
    this.periodEnd = periodEnd;
    this.status = PlanStatus.DRAFT;
    this.totalProjectedCost = BigDecimal.ZERO;
    validateInvariants();
  }

  /** Constructor completo para reconstrucción desde infraestructura. */
  public SchedulePlan(
      UUID planId,
      UUID unitId,
      LocalDate periodStart,
      LocalDate periodEnd,
      PlanStatus status,
      BigDecimal totalProjectedCost,
      List<AssignedShift> shifts) {
    this.planId = planId;
    this.unitId = unitId;
    this.periodStart = periodStart;
    this.periodEnd = periodEnd;
    this.status = status;
    this.totalProjectedCost = totalProjectedCost;
    if (shifts != null) {
      this.shifts.addAll(shifts);
    }
  }

  public void publish() {
    // En la vida real, aquí se verifica que no haya hard violations
    this.status = PlanStatus.PUBLISHED;
  }

  public void addShift(AssignedShift shift) {
    if (shift == null) {
      throw new IllegalArgumentException("Shift no puede ser nulo");
    }

    // Invariante: Contención temporal
    if (shift.getExpectedStart().toLocalDate().isBefore(this.periodStart)
        || shift.getExpectedEnd().toLocalDate().isAfter(this.periodEnd)) {
      throw new DomainRuleViolationException("El turno debe estar dentro del periodo del plan");
    }

    // Invariante: No Superposición
    boolean hasOverlap =
        shifts.stream()
            .filter(s -> s.getRelationshipId().equals(shift.getRelationshipId()) && s.isActive())
            .anyMatch(s -> s.overlapsWith(shift));

    if (hasOverlap) {
      throw new DomainRuleViolationException(
          "No se permiten turnos superpuestos para el mismo relationship_id");
    }

    shifts.add(shift);
    shift.setSchedulePlan(this);
  }

  public void removeShift(AssignedShift shift) {
    shifts.remove(shift);
    shift.setSchedulePlan(null);
  }

  public List<AssignedShift> getShifts() {
    return Collections.unmodifiableList(shifts);
  }

  private void validateInvariants() {
    if (periodStart == null || periodEnd == null) {
      throw new DomainRuleViolationException("Las fechas de inicio y fin son obligatorias");
    }
    if (!periodEnd.isAfter(periodStart)) {
      throw new DomainRuleViolationException("period_end debe ser mayor a period_start");
    }
  }
}
