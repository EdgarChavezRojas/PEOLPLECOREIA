package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.BankEntityJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: BankEntity. */
@Repository
public interface BankEntitySpringRepository extends JpaRepository<BankEntityJpa, Long> {

  Optional<BankEntityJpa> findByBankEntityIdAndTenantId(UUID bankEntityId, UUID tenantId);

  List<BankEntityJpa> findAllByTenantId(UUID tenantId);

  Optional<BankEntityJpa> findByBankCodeAndTenantId(String bankCode, UUID tenantId);
}
