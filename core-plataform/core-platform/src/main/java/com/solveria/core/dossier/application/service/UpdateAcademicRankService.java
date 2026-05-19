package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.UpdateAcademicRankCommand;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.port.TalentInventoryRepositoryPort;
import com.solveria.core.dossier.application.usecase.UpdateAcademicRankUseCase;
import com.solveria.core.dossier.domain.exception.DocumentNotFoundException;
import com.solveria.core.dossier.domain.exception.InvalidDocumentStateException;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.TalentInventory;
import com.solveria.core.dossier.domain.model.TrainingHistory;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.model.vo.ValidationState;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateAcademicRankService implements UpdateAcademicRankUseCase {

  private final DocumentRecordRepositoryPort documentRecordRepository;
  private final TalentInventoryRepositoryPort talentInventoryRepository;

  public UpdateAcademicRankService(
      DocumentRecordRepositoryPort documentRecordRepository,
      TalentInventoryRepositoryPort talentInventoryRepository) {
    this.documentRecordRepository = documentRecordRepository;
    this.talentInventoryRepository = talentInventoryRepository;
  }

  @Override
  public TalentInventory handle(UpdateAcademicRankCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    DocumentRecord record =
        documentRecordRepository
            .findById(command.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(command.documentId()));
    if (record.getDocCategory() != DocumentCategory.ACADEMIC
        || record.getValidationStatus().currentState() != ValidationState.APPROVED) {
      throw new InvalidDocumentStateException("Documento academico no validado");
    }
    TalentInventory inventory =
        talentInventoryRepository
            .findByRelationshipId(command.relationshipId())
            .orElseGet(
                () ->
                    TalentInventory.create(
                        command.relationshipId(),
                        UUID.fromString(SecurityTenantContext.getCurrentTenantId())));
    TrainingHistory training =
        new TrainingHistory(UUID.randomUUID(), command.courseName(), record.getDocId());
    inventory.addTrainingHistory(training, true);
    return talentInventoryRepository.save(inventory);
  }
}
