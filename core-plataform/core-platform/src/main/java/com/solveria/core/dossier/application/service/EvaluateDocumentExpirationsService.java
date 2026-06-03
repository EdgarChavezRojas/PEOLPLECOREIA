package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.EvaluateDocumentExpirationsCommand;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.EvaluateDocumentExpirationsUseCase;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.security.context.SecurityTenantContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EvaluateDocumentExpirationsService implements EvaluateDocumentExpirationsUseCase {

  private static final int EXPIRATION_WARNING_DAYS = 30;

  private final DocumentRecordRepositoryPort documentRecordRepository;
  private static final Logger log =
      LoggerFactory.getLogger(EvaluateDocumentExpirationsService.class);

  public EvaluateDocumentExpirationsService(DocumentRecordRepositoryPort documentRecordRepository) {
    this.documentRecordRepository = documentRecordRepository;
  }

  @Override
  public List<DocumentRecord> handle(EvaluateDocumentExpirationsCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    UUID tenantId = UUID.fromString(SecurityTenantContext.getTenantId());
    LocalDate today = command.today() != null ? command.today() : LocalDate.now();
    LocalDate warningLimit = today.plusDays(EXPIRATION_WARNING_DAYS);
    log.info("event=DOC_EXPIRATION_EVALUATION_START today={} warningLimit={}", today, warningLimit);
    List<DocumentRecord> candidates = documentRecordRepository.findExpiringOrExpired(warningLimit);
    List<DocumentRecord> updated = new ArrayList<>();
    for (DocumentRecord record : candidates) {
      if (record.getMetadata() == null || record.getMetadata().expiryDate() == null) {
        continue;
      }
      LocalDate expiryDate = record.getMetadata().expiryDate();
      if (!expiryDate.isAfter(today)) {
        record.evaluateExpiration(today, tenantId);
      } else {
        record.sendExpirationWarning();
      }
      updated.add(documentRecordRepository.save(record));

      log.info(
          "event=DOC_EXPIRATION_EVALUATION_END candidates={} updated={}",
          candidates.size(),
          updated.size());
    }
    return updated;
  }
}
