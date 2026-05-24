package com.solveria.core.legal.infrastructure.adapter;

import com.solveria.core.dossier.application.command.ComplianceDecision;
import com.solveria.core.dossier.application.command.VerifyDocumentComplianceCommand;
import com.solveria.core.dossier.application.usecase.VerifyDocumentComplianceUseCase;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.legal.application.port.DigitalKardexPort;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DigitalKardexAdapter implements DigitalKardexPort {

  private final VerifyDocumentComplianceUseCase verifyDocumentComplianceUseCase;

  // Constantes por defecto para la integración
  private static final String DEFAULT_LOCATION = "Santa Cruz";
  private static final String DEFAULT_TENANT_SEGMENT =
      "Corporativo"; // O el que aplique por defecto
  private static final String DOC_TYPE_CONTRACT = "EVIDENCIA_CONTRATO_WORM";

  public DigitalKardexAdapter(VerifyDocumentComplianceUseCase verifyDocumentComplianceUseCase) {
    this.verifyDocumentComplianceUseCase = verifyDocumentComplianceUseCase;
  }

  @Override
  public String storeEvidence(
      UUID contractId, UUID tenantId, byte[] fileContent, Instant generatedAt) {

    LocalDate expiryDate = LocalDate.ofInstant(generatedAt, ZoneId.systemDefault()).plusYears(5);

    // Construimos el Comando enviando los bytes directos sin hashear
    VerifyDocumentComplianceCommand command =
        new VerifyDocumentComplianceCommand(
            null,
            contractId,
            DocumentCategory.LEGAL,
            DOC_TYPE_CONTRACT,
            true,
            UUID.randomUUID(),
            "contract_evidence_" + contractId + ".txt",
            fileContent,
            expiryDate,
            ComplianceDecision.RECORD,
            null,
            null,
            null,
            DEFAULT_LOCATION,
            tenantId,
            DEFAULT_TENANT_SEGMENT);

    verifyDocumentComplianceUseCase.handle(command);

    // Calculamos el hash aquí en infraestructura solo para devolvérselo al BC2
    // y que lo guarde en su auditoría y eventos, manteniendo el Core limpio.
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(fileContent);
      StringBuilder hex = new StringBuilder(hashBytes.length * 2);
      for (byte b : hashBytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException("Error calculando SHA-256 de retorno", ex);
    }
  }
}
