package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.IncomeRecordJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: IncomeRecord. */
@Repository
public interface IncomeRecordSpringRepository extends JpaRepository<IncomeRecordJpa, Long> {

  Optional<IncomeRecordJpa> findByIncomeRecordIdAndTenantId(UUID incomeRecordId, UUID tenantId);

  List<IncomeRecordJpa> findAllByEmployeeIdAndPeriodRefAndTenantId(
      UUID employeeId, UUID periodRef, UUID tenantId);

  List<IncomeRecordJpa> findAllByPeriodRefAndTenantId(UUID periodRef, UUID tenantId);
}
