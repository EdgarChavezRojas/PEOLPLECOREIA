package com.solveria.core.workforce.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity: WorkerProfile
 *
 * <p>Atributos operativos para roles laborales. Evoluciona (cambios de nivel, atributos operativos)
 * y mantiene identidad vinculada a la relación laboral específica.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfile {

  private UUID profileId;
  private UUID relationshipId;
  private String employeeNo;
  private String department;
  private String jobTitle;

  public static WorkerProfile create(
      UUID relationshipId, String employeeNo, String department, String jobTitle) {
    if (relationshipId == null || employeeNo == null || employeeNo.isBlank()) {
      throw new IllegalArgumentException("relationshipId y employeeNo son requeridos");
    }

    return WorkerProfile.builder()
        .profileId(UUID.randomUUID())
        .relationshipId(relationshipId)
        .employeeNo(employeeNo)
        .department(department)
        .jobTitle(jobTitle)
        .build();
  }
}
