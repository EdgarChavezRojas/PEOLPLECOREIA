// Ruta:
// core-plataform/core-platform/src/main/java/com/solveria/core/dossier/application/service/ArchiveContractService.java
package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.ArchiveContractCommand;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.ArchiveContractUseCase;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import org.springframework.stereotype.Service;

@Service
public class ArchiveContractService implements ArchiveContractUseCase {

  private static final String DEFAULT_CONTRACT_REFERENCE = "EVIDENCIA_CONTRATO_WORM";
  private static final String PLACEHOLDER_HASH = "0".repeat(64);

  private final DocumentRecordRepositoryPort documentRecordRepository;

  public ArchiveContractService(DocumentRecordRepositoryPort documentRecordRepository) {
    this.documentRecordRepository = documentRecordRepository;
  }

  @Override
  public DocumentRecord handle(ArchiveContractCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    if (command.workerId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.RELATIONSHIP_ID_REQUIRED.name());
    }
    if (command.contractId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.DOCUMENT_ID_REQUIRED.name());
    }
    if (command.tenantId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }

    String reference =
        command.contractReference() != null && !command.contractReference().isBlank()
            ? command.contractReference()
            : DEFAULT_CONTRACT_REFERENCE;

    DocumentMetadata metadata =
        new DocumentMetadata(
            command.contractId(), "contract_" + command.contractId(), PLACEHOLDER_HASH, null);

    DocumentRecord record =
        DocumentRecord.record(
            command.workerId(),
            DocumentCategory.LEGAL,
            reference,
            true,
            metadata,
            command.tenantId());

    return documentRecordRepository.save(record);
  }
}
