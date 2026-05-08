package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.DeductionRecordJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA Repository: DeductionRecord. */
@Repository
public interface DeductionRecordSpringRepository extends JpaRepository<DeductionRecordJpa, Long> {

    Optional<DeductionRecordJpa> findByDeductionRecordIdAndTenantId(UUID deductionRecordId, String tenantId);

    List<DeductionRecordJpa> findAllByEmployeeIdAndPeriodRefAndTenantId(UUID employeeId, UUID periodRef, String tenantId);

    List<DeductionRecordJpa> findAllByPeriodRefAndTenantId(UUID periodRef, String tenantId);
}
