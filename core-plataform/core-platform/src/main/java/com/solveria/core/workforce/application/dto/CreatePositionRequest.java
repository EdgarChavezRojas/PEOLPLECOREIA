package com.solveria.core.workforce.application.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePositionRequest {

  @NotNull(message = "unitId es requerido")
  private UUID unitId;

  @NotNull(message = "jobId es requerido")
  private UUID jobId;

  @NotNull(message = "maxSlots es requerido")
  @Positive(message = "maxSlots debe ser positivo")
  private Integer maxSlots;

  private Boolean isBudgeted;
}
