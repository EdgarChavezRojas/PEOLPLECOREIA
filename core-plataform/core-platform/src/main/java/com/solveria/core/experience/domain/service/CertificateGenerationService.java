package com.solveria.core.experience.domain.service;

import com.solveria.core.experience.domain.model.vo.CertificatePayload;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Domain Service: Generación de certificados digitales (W14). Incluye hash SHA-256 y URL de
 * validación QR Zero-Trust. Clase pura sin anotaciones de infraestructura.
 */
public final class CertificateGenerationService {

  private static final String QR_VALIDATION_BASE_URL = "https://peoplecoreia.solveria.com/verify/";

  private CertificateGenerationService() {}

  /**
   * Genera un CertificatePayload con hash SHA-256 y QR Zero-Trust.
   *
   * @param certificateType Tipo de certificado (ej. "WORK_CERTIFICATE", "SALARY_CERTIFICATE")
   * @param pdfContent Contenido PDF en Base64
   * @return CertificatePayload con integridad criptográfica
   */
  public static CertificatePayload generateCertificate(String certificateType, String pdfContent) {
    if (certificateType == null || certificateType.isBlank()) {
      throw new IllegalArgumentException("Tipo de certificado requerido");
    }
    if (pdfContent == null || pdfContent.isBlank()) {
      throw new IllegalArgumentException("Contenido PDF requerido");
    }

    String sha256Hash = computeSha256(pdfContent);
    String validationToken = UUID.randomUUID().toString();
    String qrUrl = QR_VALIDATION_BASE_URL + validationToken + "?hash=" + sha256Hash;

    return CertificatePayload.create(certificateType, pdfContent, sha256Hash, qrUrl);
  }

  /** Computa hash SHA-256 del contenido para integridad criptográfica. */
  public static String computeSha256(String content) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 no disponible", e);
    }
  }
}
