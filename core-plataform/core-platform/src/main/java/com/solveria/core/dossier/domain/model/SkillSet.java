package com.solveria.core.dossier.domain.model;

import com.solveria.core.dossier.domain.model.vo.ProficiencyLevel;
import java.util.UUID;

public record SkillSet(UUID skillId, String skillName, ProficiencyLevel proficiency) {

  public SkillSet {
    if (skillId == null) {
      throw new IllegalArgumentException("skillId es requerido");
    }
    if (skillName == null || skillName.isBlank()) {
      throw new IllegalArgumentException("skillName es requerido");
    }
    if (proficiency == null) {
      throw new IllegalArgumentException("proficiency es requerido");
    }
  }
}
