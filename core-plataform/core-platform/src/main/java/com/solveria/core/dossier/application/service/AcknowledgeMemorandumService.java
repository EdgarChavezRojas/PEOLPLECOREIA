package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.AcknowledgeMemorandumCommand;
import com.solveria.core.dossier.application.port.DigitalSignaturePort;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.AcknowledgeMemorandumUseCase;
import com.solveria.core.dossier.domain.exception.DocumentNotFoundException;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.MemorandumRejectionReason;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AcknowledgeMemorandumService implements AcknowledgeMemorandumUseCase {

  private final DocumentRecordRepositoryPort documentRecordRepository;
  private final DigitalSignaturePort digitalSignaturePort;

  public AcknowledgeMemorandumService(
      DocumentRecordRepositoryPort documentRecordRepository,
      DigitalSignaturePort digitalSignaturePort) {
    this.documentRecordRepository = documentRecordRepository;
    this.digitalSignaturePort = digitalSignaturePort;
  }

  @Override
  public DocumentRecord handle(AcknowledgeMemorandumCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    LocalizationPolicy.requireSantaCruz(command.location());
    if (command.documentId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.DOCUMENT_ID_REQUIRED.name());
    }
    DocumentRecord record =
        documentRecordRepository
            .findById(command.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(command.documentId()));

    LocalDateTime acknowledgedAt =
        command.acknowledgedAt() != null ? command.acknowledgedAt() : LocalDateTime.now();

    if (command.accepted()) {
      if (command.signatureContent() == null || command.signatureContent().length == 0) {
        throw new IllegalArgumentException(DossierErrorCode.MEMORANDUM_ACK_CONTENT_REQUIRED.name());
      }
      if (command.signatureFileName() == null || command.signatureFileName().isBlank()) {
        throw new IllegalArgumentException(
            DossierErrorCode.MEMORANDUM_ACK_FILE_NAME_REQUIRED.name());
      }
      digitalSignaturePort.signAndGenerateQr(
          command.signatureContent(), command.signatureFileName(), command.signatureExpiryDate());
      record.acknowledgeMemorandum(true, command.reviewerId(), acknowledgedAt, null);
    } else {
      record.acknowledgeMemorandum(
          false,
          command.reviewerId(),
          acknowledgedAt,
          MemorandumRejectionReason.WITNESS_REQUIRED.name());
    }

    return documentRecordRepository.save(record);
  }
}
