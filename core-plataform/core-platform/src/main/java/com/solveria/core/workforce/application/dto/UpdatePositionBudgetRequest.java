package com.solveria.core.workforce.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePositionBudgetRequest {

  @NotNull(message = "isBudgeted es requerido")
  private Boolean isBudgeted;
}
