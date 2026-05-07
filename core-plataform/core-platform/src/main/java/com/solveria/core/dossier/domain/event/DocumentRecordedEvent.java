package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitido al cargar un documento al Digital Kardex.
 * Trigger: UploadToDigitalKardex.
 * Contiene el hash SHA-256 para garantizar inalterabilidad ante auditorías.
 */
public record DocumentRecordedEvent(
    UUID documentId,
    UUID personId,
    String sha256Hash,
    Instant occurredAt
) implements DomainEvent {

  public DocumentRecordedEvent {
    if (documentId == null) {
      throw new IllegalArgumentException("documentId es requerido");
    }
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido");
    }
    if (sha256Hash == null || sha256Hash.isBlank()) {
      throw new IllegalArgumentException("sha256Hash es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static DocumentRecordedEvent now(UUID documentId, UUID personId, String sha256Hash) {
    return new DocumentRecordedEvent(documentId, personId, sha256Hash, Instant.now());
  }
}
