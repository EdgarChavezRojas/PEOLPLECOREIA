package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.infrastructure.jpa.DocumentRecordJpa;
import com.solveria.core.dossier.infrastructure.mapper.DocumentRecordMapper;
import com.solveria.core.dossier.infrastructure.repository.DocumentRecordRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.workforce.application.port.EventOutboxPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentRecordRepositoryAdapter implements DocumentRecordRepositoryPort {

  private final DocumentRecordRepository documentRecordRepository;
  private final DocumentRecordMapper documentRecordMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public DocumentRecord save(DocumentRecord record) {
    List<DomainEvent> events = record.pullDomainEvents();
    DocumentRecordJpa jpa = documentRecordMapper.toJpa(record);
    DocumentRecordJpa savedJpa = documentRecordRepository.save(jpa);
    DocumentRecord saved = documentRecordMapper.toDomain(savedJpa);

    for (DomainEvent event : events) {
      if (event instanceof DossierEvent dossierEvent) {
        eventOutboxPort.publish(
            "DocumentRecord",
            saved.getDocId(),
            dossierEvent.type().name(),
            documentRecordMapper.toEventPayload(saved, dossierEvent));
      }
    }

    return saved;
  }

  @Override
  public Optional<DocumentRecord> findById(UUID docId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return documentRecordRepository
        .findByDocIdAndTenantId(docId, UUID.fromString(tenantId))
        .map(documentRecordMapper::toDomain);
  }
}
