package com.solveria.core.workforce.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminateRelationshipRequest {

  @NotBlank(message = "reason es requerido")
  private String reason;
}
