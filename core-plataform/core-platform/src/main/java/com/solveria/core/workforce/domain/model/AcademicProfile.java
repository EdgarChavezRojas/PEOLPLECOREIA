package com.solveria.core.workforce.domain.model;

import com.solveria.core.workforce.domain.model.vo.AcademicRank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity: AcademicProfile
 *
 * <p>Exclusivo para Tenant Educación. Gestiona rango académico, carga horaria y materias asignadas.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicProfile {

  private UUID academicId;
  private UUID relationshipId;
  private AcademicRank currentRank;
  private Integer teachingLoad; // Límite de carga horaria semestral

  public static AcademicProfile create(
      UUID relationshipId, AcademicRank initialRank, Integer teachingLoad) {
    if (relationshipId == null || initialRank == null || teachingLoad == null) {
      throw new IllegalArgumentException(
          "relationshipId, initialRank y teachingLoad son requeridos");
    }
    if (teachingLoad <= 0) {
      throw new IllegalArgumentException("teachingLoad debe ser mayor a 0");
    }

    return AcademicProfile.builder()
        .academicId(UUID.randomUUID())
        .relationshipId(relationshipId)
        .currentRank(initialRank)
        .teachingLoad(teachingLoad)
        .build();
  }

  public void upgradeRank(AcademicRank newRank) {
    if (newRank == null) {
      throw new IllegalArgumentException("newRank no puede ser nulo");
    }
    this.currentRank = newRank;
  }

  public void updateTeachingLoad(Integer newLoad) {
    if (newLoad == null || newLoad <= 0) {
      throw new IllegalArgumentException("Teaching load debe ser mayor a 0");
    }
    this.teachingLoad = newLoad;
  }
}
