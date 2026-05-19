package com.solveria.core.legal.infrastructure.repository;

import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.infrastructure.jpa.ContractJpa;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractRepository extends JpaRepository<ContractJpa, Long> {

  Optional<ContractJpa> findByContractIdAndTenantId(UUID contractId, String tenantId);

  @Query(
      "select distinct c from ContractJpa c join c.addendums a "
          + "where c.contractType = :contractType "
          + "and a.effectiveTo between :from and :to "
          + "and c.tenantId = :tenantId")
  List<ContractJpa> findExpiringContracts(
      @Param("contractType") ContractType contractType,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("tenantId") String tenantId);

  @Query(
      "select distinct c from ContractJpa c join c.addendums a "
          + "where c.contractType = :contractType "
          + "and a.effectiveTo = :exactDate "
          + "and c.tenantId = :tenantId")
  List<ContractJpa> findContractsExpiringExactlyOn(
      @Param("contractType") ContractType contractType,
      @Param("exactDate") LocalDate exactDate,
      @Param("tenantId") String tenantId);

    Optional<Contract> findByRelationshipId(UUID relationshipId);

  List<Contract> findByProjectId(UUID projectId);
}
