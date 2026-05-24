package com.solveria.core.workforce.application.dto;

import com.solveria.core.workforce.domain.model.vo.Extension;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelocateOrgUnitRequest {

  @NotNull(message = "geoExtension es requerido")
  private Extension geoExtension;
}
