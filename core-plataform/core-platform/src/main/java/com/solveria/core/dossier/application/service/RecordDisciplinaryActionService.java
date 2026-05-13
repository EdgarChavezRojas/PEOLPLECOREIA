package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.RecordDisciplinaryActionCommand;
import com.solveria.core.dossier.application.port.DigitalSignaturePort;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.RecordDisciplinaryActionUseCase;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RecordDisciplinaryActionService implements RecordDisciplinaryActionUseCase {

  private static final int DISCIPLINARY_THRESHOLD = 3;

  private final DocumentRecordRepositoryPort documentRecordRepository;
  private final DigitalSignaturePort digitalSignaturePort;

  public RecordDisciplinaryActionService(
      DocumentRecordRepositoryPort documentRecordRepository,
      DigitalSignaturePort digitalSignaturePort) {
    this.documentRecordRepository = documentRecordRepository;
    this.digitalSignaturePort = digitalSignaturePort;
  }

  @Override
  public DocumentRecord handle(RecordDisciplinaryActionCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    LocalizationPolicy.requireSantaCruz(command.location());
    if (command.employeeId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.EMPLOYEE_ID_REQUIRED.name());
    }
    if (command.severity() == null) {
      throw new IllegalArgumentException(DossierErrorCode.DISCIPLINARY_SEVERITY_REQUIRED.name());
    }
    if (command.reason() == null || command.reason().isBlank()) {
      throw new IllegalArgumentException(DossierErrorCode.DISCIPLINARY_REASON_REQUIRED.name());
    }
    if (command.evidenceContent() == null || command.evidenceContent().length == 0) {
      throw new IllegalArgumentException(DossierErrorCode.DISCIPLINARY_EVIDENCE_REQUIRED.name());
    }
    if (command.evidenceFileName() == null || command.evidenceFileName().isBlank()) {
      throw new IllegalArgumentException(
          DossierErrorCode.DISCIPLINARY_EVIDENCE_FILE_NAME_REQUIRED.name());
    }

    DigitalSignaturePort.SignedDocument signed =
        digitalSignaturePort.signAndGenerateQr(
            command.evidenceContent(), command.evidenceFileName(), command.evidenceExpiryDate());

    DocumentMetadata metadata =
        new DocumentMetadata(
            signed.storageId(), signed.fileName(), signed.hashSha256(), signed.expiryDate());

    DocumentRecord record =
        DocumentRecord.record(
            command.employeeId(),
            DocumentCategory.DISCIPLINARY,
            command.severity().name(),
            false,
            metadata,
            UUID.fromString(SecurityTenantContext.getCurrentTenantId()));

    long memoCount = documentRecordRepository.countDisciplinaryMemos(command.employeeId());
    record.registerDisciplinaryOutcome(memoCount >= DISCIPLINARY_THRESHOLD);
    return documentRecordRepository.save(record);
  }
}


