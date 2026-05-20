package com.solveria.core.dossier.application.command;

import com.solveria.core.dossier.domain.model.vo.ProficiencyLevel;
import java.util.UUID;

public record UpdateSkillSetCommand(
    UUID relationshipId,
    String skillName,
    ProficiencyLevel proficiency,
    String location,
    UUID tenantId) {}
