package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.UpdateSkillSetCommand;
import com.solveria.core.dossier.application.port.TalentInventoryRepositoryPort;
import com.solveria.core.dossier.application.usecase.UpdateSkillSetUseCase;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.exception.TalentInventoryNotFoundException;
import com.solveria.core.dossier.domain.model.SkillSet;
import com.solveria.core.dossier.domain.model.TalentInventory;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateSkillSetService implements UpdateSkillSetUseCase {

  private final TalentInventoryRepositoryPort talentInventoryRepository;

  public UpdateSkillSetService(TalentInventoryRepositoryPort talentInventoryRepository) {
    this.talentInventoryRepository = talentInventoryRepository;
  }

  @Override
  public TalentInventory handle(UpdateSkillSetCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    LocalizationPolicy.requireSantaCruz(command.location());
    if (command.relationshipId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.RELATIONSHIP_ID_REQUIRED.name());
    }
    if (command.skillName() == null || command.skillName().isBlank()) {
      throw new IllegalArgumentException(DossierErrorCode.SKILL_NAME_REQUIRED.name());
    }
    if (command.proficiency() == null) {
      throw new IllegalArgumentException(DossierErrorCode.PROFIENCY_REQUIRED.name());
    }
    TalentInventory inventory =
        talentInventoryRepository
            .findByRelationshipId(command.relationshipId())
            .orElseThrow(() -> new TalentInventoryNotFoundException(command.relationshipId()));

    SkillSet skillSet =
        new SkillSet(UUID.randomUUID(), command.skillName(), command.proficiency());
    inventory.addSkillSet(skillSet);
    return talentInventoryRepository.save(inventory);
  }
}


