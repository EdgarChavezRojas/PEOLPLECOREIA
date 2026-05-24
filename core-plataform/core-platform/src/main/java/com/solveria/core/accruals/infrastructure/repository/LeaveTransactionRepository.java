package com.solveria.core.accruals.infrastructure.repository;

import com.solveria.core.accruals.infrastructure.jpa.LeaveTransactionJpa;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTransactionRepository extends JpaRepository<LeaveTransactionJpa, UUID> {

  Page<LeaveTransactionJpa> findByBalanceRelationshipId(UUID relationshipId, Pageable pageable);

  Page<LeaveTransactionJpa> findByBalanceRelationshipIdAndBalanceTenantId(
      UUID relationshipId, UUID tenantId, Pageable pageable);
}
