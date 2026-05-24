package com.solveria.scheduling.domain.model.entity;

import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import com.solveria.scheduling.domain.model.enums.ShiftType;
import com.solveria.scheduling.domain.model.vo.ConstraintViolation;
import com.solveria.scheduling.domain.model.vo.ShiftMetadata;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

/** Entidad que representa un bloque de tiempo asignado a un trabajador (Relationship). */
@Getter
public class AssignedShift {

  private final UUID shiftId;
  private SchedulePlan schedulePlan;
  private final UUID relationshipId;
  private final LocalDateTime expectedStart;
  private final LocalDateTime expectedEnd;
  private final ShiftType shiftType;
  private boolean isActive;
  private ShiftMetadata metadata;
  private final List<ConstraintViolation> violations = new ArrayList<>();

  public AssignedShift(
      UUID relationshipId,
      LocalDateTime expectedStart,
      LocalDateTime expectedEnd,
      ShiftType shiftType) {
    this.shiftId = UUID.randomUUID();
    this.relationshipId = relationshipId;
    this.expectedStart = expectedStart;
    this.expectedEnd = expectedEnd;
    this.shiftType = shiftType;
    this.isActive = true;
  }

  /** Constructor completo para reconstrucción desde infraestructura. */
  public AssignedShift(
      UUID shiftId,
      UUID relationshipId,
      LocalDateTime expectedStart,
      LocalDateTime expectedEnd,
      ShiftType shiftType,
      boolean isActive,
      ShiftMetadata metadata,
      List<ConstraintViolation> violations) {
    this.shiftId = shiftId;
    this.relationshipId = relationshipId;
    this.expectedStart = expectedStart;
    this.expectedEnd = expectedEnd;
    this.shiftType = shiftType;
    this.isActive = isActive;
    this.metadata = metadata;
    if (violations != null) {
      this.violations.addAll(violations);
    }
  }

  public void setSchedulePlan(SchedulePlan schedulePlan) {
    this.schedulePlan = schedulePlan;
  }

  public void updateMetadata(ShiftMetadata metadata) {
    this.metadata = metadata;
  }

  public void addViolation(ConstraintViolation violation) {
    this.violations.add(violation);
  }

  public void cancelShift() {
    this.isActive = false;
  }

  public boolean overlapsWith(AssignedShift other) {
    if (other == null) return false;
    return this.expectedStart.isBefore(other.expectedEnd)
        && other.expectedStart.isBefore(this.expectedEnd);
  }

  public List<ConstraintViolation> getViolations() {
    return Collections.unmodifiableList(violations);
  }
}
