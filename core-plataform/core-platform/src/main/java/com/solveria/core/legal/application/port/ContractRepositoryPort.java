package com.solveria.core.legal.application.port;

import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.ContractType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepositoryPort {

  Optional<Contract> findById(UUID contractId);

  void save(Contract contract);

  List<Contract> findFixedTermContractsExpiringBetween(LocalDate from, LocalDate to);

  List<Contract> findContractsExpiringExactlyOn(
      ContractType type, LocalDate exactDate, String tenantId);
}
