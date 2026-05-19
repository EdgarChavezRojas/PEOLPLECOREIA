package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.port.DocumentRecordRepositoryPort;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.infrastructure.jpa.DocumentRecordJpa;
import com.solveria.core.dossier.infrastructure.mapper.DocumentRecordMapper;
import com.solveria.core.dossier.infrastructure.repository.DocumentRecordRepository;
import com.solveria.core.security.context.SecurityTenantContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
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
    DocumentRecordJpa jpa = documentRecordMapper.toJpa(record);
    DocumentRecordJpa savedJpa = documentRecordRepository.save(jpa);
    DocumentRecord saved = documentRecordMapper.toDomain(savedJpa);

    eventOutboxPort.publish(record.pullDomainEvents());

    return saved;
  }

  @Override
  public Optional<DocumentRecord> findById(UUID docId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return documentRecordRepository
        .findByDocIdAndTenantId(docId, UUID.fromString(tenantId))
        .map(documentRecordMapper::toDomain);
  }
  /*  ARQUITECTURA MULTI-TENANT:
   * NO utilizar SecurityTenantContext.getCurrentTenantId() en este método.
   * Este puerto es consumido principalmente por procesos Batch en segundo plano (@Scheduled)
   * que carecen de contexto de usuario. La consulta debe recuperar los registros de
   * TODOS los tenants a nivel global para procesar las expiraciones masivas.
   */
  @Override
  public List<DocumentRecord> findExpiringOrExpired(LocalDate maxExpiryDate) {
    return documentRecordRepository
        .findByExpirationWarningSentFalseAndMetadata_ExpiryDateIsNotNullAndMetadata_ExpiryDateLessThanEqual(
            maxExpiryDate)
        .stream()
        .map(documentRecordMapper::toDomain)
        .toList();
  }

  @Override
  public long countDisciplinaryMemos(UUID employeeId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return documentRecordRepository.countByRelationshipIdAndDocCategoryAndTenantId(
        employeeId, DocumentCategory.DISCIPLINARY, UUID.fromString(tenantId));
  }
}
