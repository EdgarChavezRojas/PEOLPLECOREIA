package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Certificado digital generado exitosamente (W14). Contiene referencia al hash
 * SHA-256 y URL QR Zero-Trust para validación pública.
 */
public record CertificateGeneratedEvent(
    UUID actionId,
    UUID personId,
    String certificateType,
    String sha256Hash,
    String qrValidationUrl,
    UUID tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public CertificateGeneratedEvent(
      UUID actionId,
      UUID personId,
      String certificateType,
      String sha256Hash,
      String qrValidationUrl,
      UUID tenantId) {
    this(actionId, personId, certificateType, sha256Hash, qrValidationUrl, tenantId, Instant.now());
  }
}
