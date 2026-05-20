package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.port.DigitalSignaturePort;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DigitalSignatureAdapter implements DigitalSignaturePort {

  private static final String DEFAULT_QR_BASE_URL = "https://peoplecoreia.solveria.com/verify/";

  private final String qrBaseUrl;

  public DigitalSignatureAdapter(@Value("${dossier.qr.base-url:}") String qrBaseUrl) {
    this.qrBaseUrl = normalizeBaseUrl(qrBaseUrl);
  }

  @Override
  public SignedDocument signAndGenerateQr(byte[] content, String fileName, LocalDate expiryDate) {
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("content es requerido");
    }
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("fileName es requerido");
    }

    UUID storageId = UUID.randomUUID();
    String hashSha256 = computeSha256(content);
    String qrPayload = qrBaseUrl + storageId + "?hash=" + hashSha256;
    byte[] signedContent = content.clone();

    log.info(
        "event=DOSSIER_SIGNATURE_GENERATED storageId={} fileName={} hashSha256={}",
        storageId,
        fileName,
        hashSha256);

    return new SignedDocument(storageId, fileName, hashSha256, expiryDate, qrPayload, signedContent);
  }

  private String normalizeBaseUrl(String baseUrl) {
    String resolved =
        (baseUrl == null || baseUrl.isBlank()) ? DEFAULT_QR_BASE_URL : baseUrl.trim();
    return resolved.endsWith("/") ? resolved : resolved + "/";
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

