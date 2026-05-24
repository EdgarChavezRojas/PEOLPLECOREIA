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
public class CreateOrgUnitRequest {

  @NotBlank(message = "name es requerido")
  private String name;

  @NotBlank(message = "unitType es requerido")
  private String unitType; // ADMINISTRATIVE, ACADEMIC, COMMERCIAL

  @NotBlank(message = "costCode es requerido")
  private String costCode;

  private String costDescription;

  private String geoCoords;
}
