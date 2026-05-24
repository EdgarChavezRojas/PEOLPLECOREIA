package com.solveria.core.dossier.application.port;

import java.time.LocalDate;
import java.util.UUID;

public interface DigitalSignaturePort {

  SignedDocument signAndGenerateQr(byte[] content, String fileName, LocalDate expiryDate);

  record SignedDocument(
      UUID storageId,
      String fileName,
      String hashSha256,
      LocalDate expiryDate,
      String qrPayload,
      byte[] signedContent) {}
}
