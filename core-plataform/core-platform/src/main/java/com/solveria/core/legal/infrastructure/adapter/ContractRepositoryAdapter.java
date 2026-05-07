package com.solveria.core.legal.infrastructure.adapter;

import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.infrastructure.jpa.ContractJpa;
import com.solveria.core.legal.infrastructure.mapper.ContractMapper;
import com.solveria.core.legal.infrastructure.repository.ContractRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.workforce.application.port.EventOutboxPort;
import java.time.LocalDate;
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
public class ContractRepositoryAdapter implements ContractRepositoryPort {

  private final ContractRepository contractRepository;
  private final ContractMapper contractMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public void save(Contract contract) {
    ContractJpa contractJpa = contractMapper.toJpa(contract);
    ContractJpa savedJpa = contractRepository.save(contractJpa);
    Contract savedContract = contractMapper.toDomain(savedJpa);
    List<DomainEvent> events = contract.pullDomainEvents();

    for (DomainEvent event : events) {
      eventOutboxPort.publish(
          "Contract",
          savedContract.getContractId(),
          contractMapper.resolveEventType(event),
          contractMapper.toEventPayload(savedContract, event));
    }
  }

  @Override
  public Optional<Contract> findById(UUID contractId) {
    String currentTenantId = SecurityTenantContext.getCurrentTenantId();
    return contractRepository
        .findByContractIdAndTenantId(contractId, currentTenantId)
        .map(contractMapper::toDomain);
  }

  @Override
  public List<Contract> findFixedTermContractsExpiringBetween(LocalDate from, LocalDate to) {
    String currentTenantId = SecurityTenantContext.getCurrentTenantId();
    return contractRepository
        .findExpiringContracts(ContractType.PLAZO_FIJO, from, to, currentTenantId)
        .stream()
        .map(contractMapper::toDomain)
        .toList();
  }
}
