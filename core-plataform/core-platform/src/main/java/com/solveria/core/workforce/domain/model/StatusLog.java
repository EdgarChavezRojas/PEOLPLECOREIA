package com.solveria.core.workforce.domain.model;

import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity: StatusLog
 *
 * <p>Trazabilidad inalterable de transiciones de estado. Requiere identidad para auditoría de
 * transiciones.
 */
public class StatusLog {

  private UUID logId;
  private UUID relationshipId;
  private RelationshipStatus previousStatus;
  private RelationshipStatus newStatus;
  private String changeReason;
  private LocalDate changedAt;
  private UUID changedBy;

  public StatusLog() {}

  public StatusLog(UUID logId, UUID relationshipId, RelationshipStatus previousStatus, RelationshipStatus newStatus, String changeReason, LocalDate changedAt, UUID changedBy) {
    this.logId = logId;
    this.relationshipId = relationshipId;
    this.previousStatus = previousStatus;
    this.newStatus = newStatus;
    this.changeReason = changeReason;
    this.changedAt = changedAt;
    this.changedBy = changedBy;
  }

  public UUID getLogId() { return logId; }
  public void setLogId(UUID logId) { this.logId = logId; }

  public UUID getRelationshipId() { return relationshipId; }
  public void setRelationshipId(UUID relationshipId) { this.relationshipId = relationshipId; }

  public RelationshipStatus getPreviousStatus() { return previousStatus; }
  public void setPreviousStatus(RelationshipStatus previousStatus) { this.previousStatus = previousStatus; }

  public RelationshipStatus getNewStatus() { return newStatus; }
  public void setNewStatus(RelationshipStatus newStatus) { this.newStatus = newStatus; }

  public String getChangeReason() { return changeReason; }
  public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

  public LocalDate getChangedAt() { return changedAt; }
  public void setChangedAt(LocalDate changedAt) { this.changedAt = changedAt; }

  public UUID getChangedBy() { return changedBy; }
  public void setChangedBy(UUID changedBy) { this.changedBy = changedBy; }

  public static StatusLog create(UUID relationshipId, RelationshipStatus previousStatus, RelationshipStatus newStatus, String changeReason, UUID changedBy) {
    if (relationshipId == null || newStatus == null) {
      throw new IllegalArgumentException("relationshipId, previousStatus y newStatus son requeridos");
    }
    return new StatusLog(UUID.randomUUID(), relationshipId, previousStatus, newStatus, changeReason, LocalDate.now(), changedBy);
  }
}
