package com.solveria.core.experience.domain.model.vo;

import java.time.Instant;

/**
 * Value Object: Payload de certificado digital generado (W14). Incluye hash SHA-256 para integridad
 * y URL de validación QR Zero-Trust.
 */
public record CertificatePayload(
    String certificateType,
    String pdfBase64Content,
    String sha256Hash,
    String qrValidationUrl,
    Instant generatedAt) {

  public CertificatePayload {
    if (certificateType == null || certificateType.isBlank()) {
      throw new IllegalArgumentException("El tipo de certificado no puede estar vacío");
    }
    if (sha256Hash == null || sha256Hash.isBlank()) {
      throw new IllegalArgumentException(
          "El hash SHA-256 es obligatorio para integridad del certificado");
    }
    if (qrValidationUrl == null || qrValidationUrl.isBlank()) {
      throw new IllegalArgumentException("La URL de validación QR Zero-Trust es obligatoria");
    }
    if (generatedAt == null) {
      generatedAt = Instant.now();
    }
  }

  /** Factory: crea un payload de certificado con validación completa. */
  public static CertificatePayload create(
      String certificateType, String pdfBase64Content, String sha256Hash, String qrValidationUrl) {
    return new CertificatePayload(
        certificateType, pdfBase64Content, sha256Hash, qrValidationUrl, Instant.now());
  }
}
