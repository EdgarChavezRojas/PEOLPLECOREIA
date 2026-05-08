package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.BankEntityJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA Repository: BankEntity. */
@Repository
public interface BankEntitySpringRepository extends JpaRepository<BankEntityJpa, Long> {

    Optional<BankEntityJpa> findByBankEntityIdAndTenantId(UUID bankEntityId, String tenantId);

    List<BankEntityJpa> findAllByTenantId(String tenantId);

    Optional<BankEntityJpa> findByBankCodeAndTenantId(String bankCode, String tenantId);
}
