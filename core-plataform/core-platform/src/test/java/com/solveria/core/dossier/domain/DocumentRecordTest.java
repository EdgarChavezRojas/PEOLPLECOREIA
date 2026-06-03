package com.solveria.core.dossier.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solveria.core.dossier.domain.event.DocumentRecordedEvent;
import com.solveria.core.dossier.domain.event.HealthCardExpirationWarningEvent;
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

    List<DocumentRecordedEvent> events =
        record.pullDomainEvents().stream()
            .filter(DocumentRecordedEvent.class::isInstance)
            .map(DocumentRecordedEvent.class::cast)
            .toList();

    assertEquals(1, events.size());
    assertInstanceOf(DocumentRecordedEvent.class, events.get(0));
  }

  @Test
  void evaluateExpirationEmitsWarning() {
    UUID tenantId = UUID.randomUUID();
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

    record.evaluateExpiration(LocalDate.now(), tenantId);

    boolean hasWarning =
        record.pullDomainEvents().stream()
            .anyMatch(HealthCardExpirationWarningEvent.class::isInstance);

    assertTrue(hasWarning);
  }
}
