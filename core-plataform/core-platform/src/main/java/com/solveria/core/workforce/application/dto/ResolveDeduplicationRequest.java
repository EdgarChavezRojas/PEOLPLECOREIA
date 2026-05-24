package com.solveria.core.workforce.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveDeduplicationRequest {

  @NotNull(message = "duplicateId es requerido")
  private UUID duplicateId;
}
