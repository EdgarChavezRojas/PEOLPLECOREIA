package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.GenerateDigitalCertificateCommand;
import com.solveria.core.dossier.application.port.DigitalSignaturePort;
import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.application.usecase.GenerateDigitalCertificateUseCase;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.UUID;

public class GenerateDigitalCertificateService implements GenerateDigitalCertificateUseCase {

  private final DocumentRecordRepositoryPort documentRecordRepository;
  private final DigitalSignaturePort digitalSignaturePort;

  public GenerateDigitalCertificateService(
      DocumentRecordRepositoryPort documentRecordRepository,
      DigitalSignaturePort digitalSignaturePort) {
    this.documentRecordRepository = documentRecordRepository;
    this.digitalSignaturePort = digitalSignaturePort;
  }

  @Override
  public DocumentRecord handle(GenerateDigitalCertificateCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    DigitalSignaturePort.SignedDocument signed =
        digitalSignaturePort.signAndGenerateQr(
            command.content(), command.fileName(), command.expiryDate());
    DocumentMetadata metadata =
        new DocumentMetadata(
            signed.storageId(), signed.fileName(), signed.hashSha256(), signed.expiryDate());
    DocumentRecord record =
        DocumentRecord.record(
            command.relationshipId(),
            command.category(),
            command.docType(),
            command.critical(),
            metadata,
            UUID.fromString(SecurityTenantContext.getCurrentTenantId()));
    return documentRecordRepository.save(record);
  }
}
