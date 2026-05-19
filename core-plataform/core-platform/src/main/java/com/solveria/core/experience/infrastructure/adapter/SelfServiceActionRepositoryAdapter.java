package com.solveria.core.experience.infrastructure.adapter;

import com.solveria.core.experience.application.port.out.SelfServiceActionPO;
import com.solveria.core.experience.domain.model.SelfServiceAction;
import com.solveria.core.experience.infrastructure.jpa.SelfServiceActionJpa;
import com.solveria.core.experience.infrastructure.mapper.SelfServiceActionMapper;
import com.solveria.core.experience.infrastructure.repository.SelfServiceActionRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: SelfServiceActionPO. Persiste el agregado SelfServiceAction y publica eventos al outbox.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SelfServiceActionRepositoryAdapter implements SelfServiceActionPO {

  private final SelfServiceActionRepository repository;
  private final SelfServiceActionMapper mapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public void save(SelfServiceAction action) {
    List<DomainEvent> events = action.pullDomainEvents();
    SelfServiceActionJpa jpa = mapper.toJpa(action);
     repository.save(jpa);
    eventOutboxPort.publish(events);
  }

  @Override
  public Optional<SelfServiceAction> findById(UUID actionId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return repository.findByActionIdAndTenantId(actionId, tenantId).map(mapper::toDomain);
  }

  @Override
  public List<SelfServiceAction> findByPersonId(UUID personId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return repository.findByPersonIdAndTenantId(personId, tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
