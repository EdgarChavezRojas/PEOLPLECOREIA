package com.solveria.scheduling.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sch_assigned_shift")
public class AssignedShiftJpa extends BaseEntity {

  @Column(name = "shift_id", updatable = false, columnDefinition = "UUID")
  private UUID shiftId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", nullable = false)
  private SchedulePlanJpa schedulePlan;

  @Column(name = "relationship_id", nullable = false)
  private UUID relationshipId;

  @Column(name = "expected_start", nullable = false)
  private LocalDateTime expectedStart;

  @Column(name = "expected_end", nullable = false)
  private LocalDateTime expectedEnd;

  @Column(name = "shift_type", nullable = false)
  private String shiftType;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private String metadata;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "violations", columnDefinition = "jsonb")
  private String violations;

  public UUID getShiftId() {
    return shiftId;
  }

  public void setShiftId(UUID shiftId) {
    this.shiftId = shiftId;
  }

  public SchedulePlanJpa getSchedulePlan() {
    return schedulePlan;
  }

  public void setSchedulePlan(SchedulePlanJpa schedulePlan) {
    this.schedulePlan = schedulePlan;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public void setRelationshipId(UUID relationshipId) {
    this.relationshipId = relationshipId;
  }

  public LocalDateTime getExpectedStart() {
    return expectedStart;
  }

  public void setExpectedStart(LocalDateTime expectedStart) {
    this.expectedStart = expectedStart;
  }

  public LocalDateTime getExpectedEnd() {
    return expectedEnd;
  }

  public void setExpectedEnd(LocalDateTime expectedEnd) {
    this.expectedEnd = expectedEnd;
  }

  public String getShiftType() {
    return shiftType;
  }

  public void setShiftType(String shiftType) {
    this.shiftType = shiftType;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public String getViolations() {
    return violations;
  }

  public void setViolations(String violations) {
    this.violations = violations;
  }
}
