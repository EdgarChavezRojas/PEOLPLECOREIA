package com.solveria.core.workforce.domain.model;

import com.solveria.core.workforce.domain.model.vo.AcademicRank;
import java.util.UUID;

/**
 * Entity: AcademicProfile
 *
 * <p>Exclusivo para Tenant Educación. Gestiona rango académico, carga horaria y materias asignadas.
 */
public class AcademicProfile {

  private UUID academicId;
  private UUID relationshipId;
  private AcademicRank currentRank;
  private Integer teachingLoad;

  public AcademicProfile() {}

  public AcademicProfile(
      UUID academicId, UUID relationshipId, AcademicRank currentRank, Integer teachingLoad) {
    this.academicId = academicId;
    this.relationshipId = relationshipId;
    this.currentRank = currentRank;
    this.teachingLoad = teachingLoad;
  }

  public UUID getAcademicId() {
    return academicId;
  }

  public void setAcademicId(UUID academicId) {
    this.academicId = academicId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public void setRelationshipId(UUID relationshipId) {
    this.relationshipId = relationshipId;
  }

  public AcademicRank getCurrentRank() {
    return currentRank;
  }

  public void setCurrentRank(AcademicRank currentRank) {
    this.currentRank = currentRank;
  }

  public Integer getTeachingLoad() {
    return teachingLoad;
  }

  public void setTeachingLoad(Integer teachingLoad) {
    this.teachingLoad = teachingLoad;
  }

  public static AcademicProfile create(
      UUID relationshipId, AcademicRank initialRank, Integer teachingLoad) {
    if (relationshipId == null || initialRank == null || teachingLoad == null) {
      throw new IllegalArgumentException(
          "relationshipId, initialRank y teachingLoad son requeridos");
    }
    if (teachingLoad <= 0) {
      throw new IllegalArgumentException("teachingLoad debe ser mayor a 0");
    }
    return new AcademicProfile(UUID.randomUUID(), relationshipId, initialRank, teachingLoad);
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
