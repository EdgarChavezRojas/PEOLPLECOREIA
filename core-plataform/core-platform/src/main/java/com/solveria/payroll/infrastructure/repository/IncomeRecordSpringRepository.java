package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.IncomeRecordJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA Repository: IncomeRecord. */
@Repository
public interface IncomeRecordSpringRepository extends JpaRepository<IncomeRecordJpa, Long> {

    Optional<IncomeRecordJpa> findByIncomeRecordIdAndTenantId(UUID incomeRecordId, String tenantId);

    List<IncomeRecordJpa> findAllByEmployeeIdAndPeriodRefAndTenantId(UUID employeeId, UUID periodRef, String tenantId);

    List<IncomeRecordJpa> findAllByPeriodRefAndTenantId(UUID periodRef, String tenantId);
}
