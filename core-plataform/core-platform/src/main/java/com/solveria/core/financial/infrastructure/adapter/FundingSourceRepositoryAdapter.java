package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.port.EventOutboxPort;
import com.solveria.core.financial.application.port.FundingSourceRepositoryPort;
import com.solveria.core.financial.domain.model.FundingSource;
import com.solveria.core.financial.infrastructure.jpa.FundingSourceJpa;
import com.solveria.core.financial.infrastructure.mapper.FundingSourceMapper;
import com.solveria.core.financial.infrastructure.repository.FundingSourceRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: FundingSourceRepositoryPort. Persiste el agregado FundingSource y publica eventos de
 * dominio al outbox.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FundingSourceRepositoryAdapter implements FundingSourceRepositoryPort {

  private final FundingSourceRepository fundingSourceRepository;
  private final FundingSourceMapper fundingSourceMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public void save(FundingSource fundingSource) {
    List<DomainEvent> events = fundingSource.pullDomainEvents();
    FundingSourceJpa jpa = fundingSourceMapper.toJpa(fundingSource);
    FundingSourceJpa savedJpa = fundingSourceRepository.save(jpa);
    FundingSource saved = fundingSourceMapper.toDomain(savedJpa);

    for (DomainEvent event : events) {
      eventOutboxPort.publish(
          "FundingSource",
          saved.getSourceId(),
          fundingSourceMapper.resolveEventType(event),
          fundingSourceMapper.toEventPayload(saved, event));
    }
  }

  @Override
  public Optional<FundingSource> findById(UUID sourceId) {
    String currentTenantId = SecurityTenantContext.getCurrentTenantId();
    return fundingSourceRepository
        .findBySourceIdAndTenantId(sourceId, currentTenantId)
        .map(fundingSourceMapper::toDomain);
  }

  @Override
  public Optional<FundingSource> findByProjectCode(String projectCode, String tenantId) {
    return fundingSourceRepository
        .findByProjectCodeAndTenantId(projectCode, tenantId)
        .map(fundingSourceMapper::toDomain);
  }
}
