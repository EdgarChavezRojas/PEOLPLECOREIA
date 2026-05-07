package com.solveria.core.accruals.infrastructure.repository;

import com.solveria.core.accruals.infrastructure.jpa.AccrualBalanceJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccrualBalanceRepository extends JpaRepository<AccrualBalanceJpa, UUID> {

  Optional<AccrualBalanceJpa> findByBalanceIdAndTenantId(UUID balanceId, UUID tenantId);
}
