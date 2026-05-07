package com.solveria.core.workforce.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRelationshipRequest {

  @NotNull(message = "personId es requerido")
  private UUID personId;

  @NotNull(message = "tenantId es requerido")
  private UUID tenantId;

  @NotNull(message = "relationType es requerido")
  private String relationType; // LABOR, ACADEMIC, INTERNSHIP

  private LocalDate hireDate;

  // Para WorkerProfile
  private String employeeNo;
  private String department;
  private String jobTitle;

  // Para AcademicProfile
  private Integer teachingLoad;
}
