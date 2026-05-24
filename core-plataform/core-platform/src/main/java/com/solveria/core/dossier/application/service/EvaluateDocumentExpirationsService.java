package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.EvaluateDocumentExpirationsCommand;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.EvaluateDocumentExpirationsUseCase;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EvaluateDocumentExpirationsService implements EvaluateDocumentExpirationsUseCase {

  private static final int EXPIRATION_WARNING_DAYS = 30;

  private final DocumentRecordRepositoryPort documentRecordRepository;

  public EvaluateDocumentExpirationsService(DocumentRecordRepositoryPort documentRecordRepository) {
    this.documentRecordRepository = documentRecordRepository;
  }

  @Override
  public List<DocumentRecord> handle(EvaluateDocumentExpirationsCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    LocalDate today = command.today() != null ? command.today() : LocalDate.now();
    LocalDate warningLimit = today.plusDays(EXPIRATION_WARNING_DAYS);

    List<DocumentRecord> candidates = documentRecordRepository.findExpiringOrExpired(warningLimit);
    List<DocumentRecord> updated = new ArrayList<>();
    for (DocumentRecord record : candidates) {
      if (record.getMetadata() == null || record.getMetadata().expiryDate() == null) {
        continue;
      }
      LocalDate expiryDate = record.getMetadata().expiryDate();
      if (!expiryDate.isAfter(today)) {
        record.evaluateExpiration(today);
      } else {
        record.sendExpirationWarning();
      }
      updated.add(documentRecordRepository.save(record));
    }
    return updated;
  }
}
