// Ruta:
// core-plataform/core-platform/src/main/java/com/solveria/core/dossier/application/service/CreateDocumentRequirementsService.java
package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.CreateDocumentRequirementsCommand;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.CreateDocumentRequirementsUseCase;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateDocumentRequirementsService implements CreateDocumentRequirementsUseCase {

  private static final String DOC_TYPE_HEALTH_CARD = "Carnet Sanitario";
  private static final String DOC_TYPE_ACADEMIC_TITLE = "Titulo Academico";
  private static final String PLACEHOLDER_HASH = "0".repeat(64);

  private final DocumentRecordRepositoryPort documentRecordRepository;

  public CreateDocumentRequirementsService(DocumentRecordRepositoryPort documentRecordRepository) {
    this.documentRecordRepository = documentRecordRepository;
  }

  @Override
  public List<DocumentRecord> handle(CreateDocumentRequirementsCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    if (command.workerId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.EMPLOYEE_ID_REQUIRED.name());
    }
    if (command.tenantId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }

    // Evita duplicar requerimientos documentales si ya fueron creados previamente
    long existingHealth =
        documentRecordRepository.countByRelationshipIdAndDocCategoryAndTenantId(
            command.workerId(), DocumentCategory.HEALTH, command.tenantId());
    if (existingHealth > 0) {
      return List.of();
    }

    List<DocumentRecord> baseRequirements =
        List.of(
            createPending(
                command.workerId(),
                DocumentCategory.HEALTH,
                DOC_TYPE_HEALTH_CARD,
                command.tenantId()),
            createPending(
                command.workerId(),
                DocumentCategory.ACADEMIC,
                DOC_TYPE_ACADEMIC_TITLE,
                command.tenantId()));

    List<DocumentRecord> saved = new ArrayList<>();
    for (DocumentRecord record : baseRequirements) {
      saved.add(documentRecordRepository.save(record));
    }
    return saved;
  }

  private DocumentRecord createPending(
      UUID workerId, DocumentCategory category, String docType, UUID tenantId) {
    DocumentMetadata metadata =
        new DocumentMetadata(
            UUID.randomUUID(), "pending_" + docType.replace(" ", "_"), PLACEHOLDER_HASH, null);
    return DocumentRecord.record(workerId, category, docType, false, metadata, tenantId);
  }
}
