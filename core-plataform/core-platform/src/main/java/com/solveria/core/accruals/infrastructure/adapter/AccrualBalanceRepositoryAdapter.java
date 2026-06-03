package com.solveria.core.accruals.infrastructure.adapter;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import com.solveria.core.accruals.infrastructure.jpa.AccrualBalanceJpa;
import com.solveria.core.accruals.infrastructure.jpa.LeaveTransactionJpa;
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

    // 1. Recuperamos la entidad gestionada por Hibernate
    AccrualBalanceJpa jpaToSave =
        accrualBalanceRepository
            .findById(balance.getBalanceId())
            .orElseGet(() -> accrualBalanceMapper.toJpa(balance));

    // 2. Actualizamos campos básicos
    jpaToSave.setCurrentBalance(balance.getCurrentBalance());
    jpaToSave.setDaysAccruedYtd(balance.getDaysAccruedYtd());
    jpaToSave.setDaysTakenYtd(balance.getDaysTakenYtd());
    jpaToSave.setLastAccrualDate(balance.getLastAccrualDate());

    // 3. Sincronizamos transacciones de forma segura
    if (balance.getLeaveTransactions() != null) {
      List<LeaveTransactionJpa> currentTransactions = jpaToSave.getLeaveTransactions();

      // Eliminamos las que ya no están
      currentTransactions.removeIf(
          txJpa ->
              balance.getLeaveTransactions().stream()
                  .noneMatch(d -> d.getTransactionId().equals(txJpa.getTransactionId())));

      // Actualizamos existentes o añadimos nuevas
      for (LeaveTransaction domainTx : balance.getLeaveTransactions()) {
        Optional<LeaveTransactionJpa> existingJpa =
            currentTransactions.stream()
                .filter(tx -> tx.getTransactionId().equals(domainTx.getTransactionId()))
                .findFirst();

        if (existingJpa.isPresent()) {
          accrualBalanceMapper.updateJpa(domainTx, existingJpa.get());
        } else {
          LeaveTransactionJpa newTxJpa = accrualBalanceMapper.toJpa(domainTx);
          newTxJpa.setBalance(jpaToSave);
          currentTransactions.add(newTxJpa);
        }
      }
    }

    AccrualBalanceJpa savedJpa = accrualBalanceRepository.save(jpaToSave);
    eventOutboxPort.publish(events);
    return accrualBalanceMapper.toDomain(savedJpa);
  }

  @Override
  public Optional<AccrualBalance> findById(UUID balanceId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      return accrualBalanceRepository.findById(balanceId).map(accrualBalanceMapper::toDomain);
    }
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
  @Transactional(readOnly = true)
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

  @Override
  @Transactional(readOnly = true)
  public Page<LeaveTransaction> findAllLeaves(Pageable pageable) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      return leaveTransactionRepository.findAll(pageable).map(accrualBalanceMapper::toDomain);
    }
    return leaveTransactionRepository
        .findByTenantId(UUID.fromString(tenantId), pageable)
        .map(accrualBalanceMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LeaveTransaction> findLeavesByStatus(
      com.solveria.core.accruals.domain.model.vo.LeaveStatus status, Pageable pageable) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      return leaveTransactionRepository.findAll(pageable).map(accrualBalanceMapper::toDomain);
    }
    return leaveTransactionRepository
        .findByTenantIdAndStatus(UUID.fromString(tenantId), status, pageable)
        .map(accrualBalanceMapper::toDomain);
  }
}
