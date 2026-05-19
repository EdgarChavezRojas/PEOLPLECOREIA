package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.ComplianceDecision;
import com.solveria.core.dossier.application.command.VerifyDocumentComplianceCommand;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.VerifyDocumentComplianceUseCase;

import com.solveria.core.dossier.domain.event.MandatoryComplianceDocMissingEvent;
import com.solveria.core.dossier.domain.exception.DocumentNotFoundException;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import com.solveria.core.dossier.domain.policy.DocumentCompliancePolicy;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class VerifyDocumentComplianceService implements VerifyDocumentComplianceUseCase {

  private final DocumentRecordRepositoryPort documentRecordRepository;
  private final EventOutboxPort eventOutboxPort;


  public VerifyDocumentComplianceService(
      DocumentRecordRepositoryPort documentRecordRepository, EventOutboxPort eventOutboxPort) {
    this.documentRecordRepository = documentRecordRepository;
    this.eventOutboxPort = eventOutboxPort;
  }

  @Override
  public DocumentRecord handle(VerifyDocumentComplianceCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    DocumentCompliancePolicy.requireCriticalDocumentType(
        command.tenantSegment(), command.docCategory(), command.docType(), command.critical());

    ComplianceDecision decision = command.decision();
    if (decision == null) {
      throw new IllegalArgumentException("decision es requerido");
    }

    return switch (decision) {
      case RECORD -> recordDocument(command);
      case APPROVE -> approveDocument(command);
      case REJECT -> rejectDocument(command);
      case EXPIRE -> expireDocument(command);
      case RESTORE -> restoreDocument(command);
    };
  }

  private DocumentRecord recordDocument(VerifyDocumentComplianceCommand command) {
    if (command.fileContent() == null || command.fileContent().length == 0) {
      throw new IllegalArgumentException("fileContent es requerido");
    }
    String hash = computeSha256(command.fileContent());
    DocumentMetadata metadata =
        new DocumentMetadata(command.storageId(), command.fileName(), hash, command.expiryDate());
    DocumentRecord record =
        DocumentRecord.record(
            command.relationshipId(),
            command.docCategory(),
            command.docType(),
            command.critical(),
            metadata,
            UUID.fromString(SecurityTenantContext.getCurrentTenantId()));
    record.evaluateExpiration(LocalDate.now());
    return documentRecordRepository.save(record);
  }

  private DocumentRecord approveDocument(VerifyDocumentComplianceCommand command) {
    DocumentRecord record = findOrPublishMissing(command);
    LocalDateTime reviewDate =
        command.reviewDate() != null ? command.reviewDate() : LocalDateTime.now();
    record.approve(command.reviewerId(), reviewDate);
    record.evaluateExpiration(LocalDate.now());
    return documentRecordRepository.save(record);
  }

  private DocumentRecord rejectDocument(VerifyDocumentComplianceCommand command) {
    DocumentRecord record = findOrPublishMissing(command);
    LocalDateTime reviewDate =
        command.reviewDate() != null ? command.reviewDate() : LocalDateTime.now();
    record.reject(command.reviewerId(), command.rejectReason(), reviewDate);
    return documentRecordRepository.save(record);
  }

  private DocumentRecord expireDocument(VerifyDocumentComplianceCommand command) {
    DocumentRecord record = findOrPublishMissing(command);
    record.evaluateExpiration(LocalDate.now());
    return documentRecordRepository.save(record);
  }

  private DocumentRecord restoreDocument(VerifyDocumentComplianceCommand command) {
    DocumentRecord record = findOrPublishMissing(command);
    LocalDateTime reviewDate =
        command.reviewDate() != null ? command.reviewDate() : LocalDateTime.now();
    record.restore(command.reviewerId(), reviewDate);
    return documentRecordRepository.save(record);
  }

  private DocumentRecord findOrPublishMissing(VerifyDocumentComplianceCommand command) {
    if (command.docId() == null) {
      publishMissingDocEvent(command);
      throw new IllegalArgumentException("docId es requerido");
    }
    return documentRecordRepository
        .findById(command.docId())
        .orElseThrow(
            () -> {
              publishMissingDocEvent(command);
              return new DocumentNotFoundException(command.docId());
            });
  }

  private void publishMissingDocEvent(VerifyDocumentComplianceCommand command) {
    UUID aggregateId = command.relationshipId() != null ? command.relationshipId() : UUID.randomUUID();

    // Instanciamos el nuevo evento
    MandatoryComplianceDocMissingEvent event = MandatoryComplianceDocMissingEvent.now(aggregateId);

    // Publicamos a través del puerto
    eventOutboxPort.publish(List.of(event));
  }



  private String computeSha256(byte[] input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input);
      return HexFormat.of().formatHex(hash);
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo calcular hash SHA-256", e);
    }
  }
}
