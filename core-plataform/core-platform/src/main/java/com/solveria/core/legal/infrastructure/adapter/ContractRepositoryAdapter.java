package com.solveria.core.legal.infrastructure.adapter;

import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.infrastructure.jpa.ContractJpa;
import com.solveria.core.legal.infrastructure.mapper.ContractMapper;
import com.solveria.core.legal.infrastructure.repository.ContractRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
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
    ContractJpa contractJpa =
        contractRepository
            .findByContractId(contract.getContractId())
            .map(
                existing -> {
                  contractMapper.updateJpa(existing, contract);
                  contractMapper.setBackReference(existing, contract);
                  return existing;
                })
            .orElseGet(() -> contractMapper.toJpa(contract));

    contractRepository.save(contractJpa);
    eventOutboxPort.publish(contract.pullDomainEvents());
  }

  @Override
  public Optional<Contract> findById(UUID contractId) {
    String tenantStr = SecurityTenantContext.getCurrentTenantId();
    if (tenantStr == null || tenantStr.isBlank()) {
      return contractRepository.findByContractId(contractId).map(contractMapper::toDomain);
    }
    UUID currentTenantId = UUID.fromString(tenantStr);
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

  @Override
  public List<Contract> findContractsExpiringExactlyOn(
      ContractType type, LocalDate exactDate, UUID tenantId) {
    return contractRepository.findContractsExpiringExactlyOn(type, exactDate, tenantId).stream()
        .map(contractMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<ContractJpa> findByRelationshipId(UUID relationId) {
    return contractRepository.findByRelationshipId(relationId);
  }

  @Override
  public List<ContractJpa> findByProjectId(UUID projectId) {
    return contractRepository.findByProjectId(projectId);
  }
}
