package com.solveria.core.experience.infrastructure.adapter;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.experience.application.port.out.SelfServiceActionPO;
import com.solveria.core.experience.domain.model.SelfServiceAction;
import com.solveria.core.experience.infrastructure.jpa.SelfServiceActionJpa;
import com.solveria.core.experience.infrastructure.mapper.SelfServiceActionMapper;
import com.solveria.core.experience.infrastructure.repository.SelfServiceActionRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
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
  private final RelationshipRepositoryPort relationshipRepositoryPort;
  private final AccrualBalanceRepositoryPort accrualBalanceRepositoryPort;
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
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return repository.findByActionIdAndTenantId(actionId, tenantId).map(mapper::toDomain);
  }

  @Override
  public List<SelfServiceAction> findByPersonId(UUID personId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return repository.findByPersonIdAndTenantId(personId, tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public BigDecimal getAvailableLeaveBalance(UUID personId) {
    log.info("event=EXP_BALANCE_QUERY_DELEGATED personId={}", personId);

    // 1. Obtener la relación laboral activa (Workforce BC)
    List<Relationship> relationships = relationshipRepositoryPort.findByPersonId(personId);
    Optional<Relationship> activeRelationship = relationships.stream()
            .filter(rel -> rel.getCurrentStatus() == RelationshipStatus.ACTIVE)
            .findFirst();

    if (activeRelationship.isEmpty()) {
      log.warn("event=EXP_BALANCE_QUERY_FAILED reason=NO_ACTIVE_RELATIONSHIP personId={}", personId);
      return BigDecimal.ZERO;
    }

    UUID relationshipId = activeRelationship.get().getRelationshipId();

    // 2. Obtener el saldo de vacaciones de esa relación (Accruals BC)
    List<AccrualBalance> balances = accrualBalanceRepositoryPort.findAllByRelationshipId(relationshipId);

    return balances.stream()
            .filter(bal -> bal.getBalanceType() == AccrualBalanceType.VACATION)
            .map(AccrualBalance::getCurrentBalance)
            .findFirst()
            .orElse(BigDecimal.ZERO);
  }
  }

