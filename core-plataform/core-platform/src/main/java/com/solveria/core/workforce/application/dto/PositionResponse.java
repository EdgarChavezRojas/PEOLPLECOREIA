package com.solveria.core.workforce.application.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionResponse {

  private UUID positionId;
  private UUID unitId;
  private UUID jobId;
  private String status; // VACANT, OCCUPIED, RESERVED
  private Boolean isBudgeted;
  private Integer maxSlots;
  private Integer currentSlots;
  private Integer availableSlots;
  private java.util.List<UUID> occupantPersonIds;
}
