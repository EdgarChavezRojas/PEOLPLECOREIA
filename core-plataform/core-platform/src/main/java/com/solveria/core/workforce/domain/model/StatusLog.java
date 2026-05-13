package com.solveria.core.workforce.domain.model;

import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity: StatusLog
 *
 * <p>Trazabilidad inalterable de transiciones de estado. Requiere identidad para auditoría de
 * transiciones.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusLog {

  private UUID logId;
  private UUID relationshipId;
  private RelationshipStatus previousStatus;
  private RelationshipStatus newStatus;
  private String changeReason;
  private LocalDate changedAt;
  private UUID changedBy;

  public static StatusLog create(
      UUID relationshipId,
      RelationshipStatus previousStatus,
      RelationshipStatus newStatus,
      String changeReason,
      UUID changedBy) {
    if (relationshipId == null || newStatus == null) {
      throw new IllegalArgumentException(
          "relationshipId, previousStatus y newStatus son requeridos");
    }

    return StatusLog.builder()
        .logId(UUID.randomUUID())
        .relationshipId(relationshipId)
        .previousStatus(previousStatus)
        .newStatus(newStatus)
        .changeReason(changeReason)
        .changedAt(LocalDate.now())
        .changedBy(changedBy)
        .build();
  }
}
