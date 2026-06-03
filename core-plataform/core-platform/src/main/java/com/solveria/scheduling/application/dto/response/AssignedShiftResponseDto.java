package com.solveria.scheduling.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO de respuesta para representar los detalles individuales de un turno asignado. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedShiftResponseDto {

  private UUID shiftId;
  private UUID relationshipId;
  private LocalDateTime expectedStart;
  private LocalDateTime expectedEnd;
  private String shiftType;
  private boolean isActive;
  private String metadata;
  private String violations;
}
