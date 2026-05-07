package com.solveria.core.dossier.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.event.DossierEventType;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentRecordTest {

  @Test
  void recordEmitsDocumentRecordedEvent() {
    DocumentMetadata metadata =
        new DocumentMetadata(
            UUID.randomUUID(), "archivo.pdf", "a".repeat(64), LocalDate.now().plusDays(10));
    DocumentRecord record =
        DocumentRecord.record(
            UUID.randomUUID(),
            DocumentCategory.HEALTH,
            "Carnet Sanitario",
            true,
            metadata,
            UUID.randomUUID());

    List<DossierEvent> events =
        record.pullDomainEvents().stream()
            .filter(DossierEvent.class::isInstance)
            .map(DossierEvent.class::cast)
            .toList();

    assertEquals(1, events.size());
    assertEquals(DossierEventType.DOCUMENT_RECORDED, events.get(0).type());
  }

  @Test
  void evaluateExpirationEmitsWarning() {
    DocumentMetadata metadata =
        new DocumentMetadata(
            UUID.randomUUID(), "archivo.pdf", "b".repeat(64), LocalDate.now().plusDays(5));
    DocumentRecord record =
        DocumentRecord.record(
            UUID.randomUUID(),
            DocumentCategory.HEALTH,
            "Carnet Sanitario",
            true,
            metadata,
            UUID.randomUUID());

    record.evaluateExpiration(LocalDate.now());

    boolean hasWarning =
        record.pullDomainEvents().stream()
            .filter(DossierEvent.class::isInstance)
            .map(DossierEvent.class::cast)
            .anyMatch(event -> event.type() == DossierEventType.HEALTH_CARD_EXPIRATION_WARNING);

    assertTrue(hasWarning);
  }
}
