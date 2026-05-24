package com.solveria.core.accruals.infrastructure.adapter;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import com.solveria.core.accruals.infrastructure.jpa.AccrualBalanceJpa;
import com.solveria.core.accruals.infrastructure.mapper.AccrualBalanceMapper;
import com.solveria.core.accruals.infrastructure.repository.AccrualBalanceRepository;
import com.solveria.core.accruals.infrastructure.repository.LeaveTransactionRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AccrualBalanceRepositoryAdapter implements AccrualBalanceRepositoryPort {

  private final AccrualBalanceRepository accrualBalanceRepository;
  private final AccrualBalanceMapper accrualBalanceMapper;
  private final EventOutboxPort eventOutboxPort;
  private final LeaveTransactionRepository leaveTransactionRepository;

  @Override
  @Transactional
  public AccrualBalance save(AccrualBalance balance) {
    List<DomainEvent> events = balance.pullDomainEvents();
    AccrualBalanceJpa jpa = accrualBalanceMapper.toJpa(balance);
    AccrualBalanceJpa savedJpa = accrualBalanceRepository.save(jpa);
    AccrualBalance saved = accrualBalanceMapper.toDomain(savedJpa);

    eventOutboxPort.publish(events);

    return saved;
  }

  @Override
  public Optional<AccrualBalance> findById(UUID balanceId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return accrualBalanceRepository
        .findByBalanceIdAndTenantId(balanceId, UUID.fromString(tenantId))
        .map(accrualBalanceMapper::toDomain);
  }

  @Override
  public List<AccrualBalance> findAll() {
    return accrualBalanceRepository.findAll().stream().map(accrualBalanceMapper::toDomain).toList();
  }

  @Override
  public List<AccrualBalance> findAllByRelationshipId(UUID relationshipId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      return accrualBalanceRepository.findByRelationshipId(relationshipId).stream()
          .map(accrualBalanceMapper::toDomain)
          .toList();
    }
    return accrualBalanceRepository
        .findByRelationshipIdAndTenantId(relationshipId, UUID.fromString(tenantId))
        .stream()
        .map(accrualBalanceMapper::toDomain)
        .toList();
  }

  @Override
  public Page<LeaveTransaction> findLeaveTransactionsByRelationshipId(
      UUID relationshipId, Pageable pageable) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      return leaveTransactionRepository
          .findByBalanceRelationshipId(relationshipId, pageable)
          .map(accrualBalanceMapper::toDomain);
    }
    return leaveTransactionRepository
        .findByBalanceRelationshipIdAndBalanceTenantId(
            relationshipId, UUID.fromString(tenantId), pageable)
        .map(accrualBalanceMapper::toDomain);
  }
}
