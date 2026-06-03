package com.solveria.core.workforce.application.dto;

import com.solveria.core.workforce.domain.model.vo.AcademicRank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmployeeAcademicProfileRequest {

  @NotNull(message = "newRank es requerido")
  private AcademicRank newRank;
}
