package com.solveria.core.workforce.application.dto.webRequest;

import com.solveria.core.workforce.application.dto.CreateRelationshipRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRelationshipWebDto(
    @NotNull(message = "personId es requerido") UUID personId,
    @NotBlank(message = "relationType es requerido")
        String relationType, // LABOR, ACADEMIC, INTERNSHIP
    LocalDate hireDate,

    // Para WorkerProfile
    String employeeNo,
    String department,
    String jobTitle,

    // Para AcademicProfile
    Integer teachingLoad) {
  // Método Factory para crear el Command inyectando el tenantId seguro
  public CreateRelationshipRequest toCommand(UUID tenantId) {
    return new CreateRelationshipRequest(
        this.personId(),
        tenantId,
        this.relationType(),
        this.hireDate(),
        this.employeeNo(),
        this.department(),
        this.jobTitle(),
        this.teachingLoad());
  }
}
