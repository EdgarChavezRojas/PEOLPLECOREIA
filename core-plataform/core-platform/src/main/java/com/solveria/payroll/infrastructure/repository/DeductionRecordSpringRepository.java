package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.DeductionRecordJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: DeductionRecord. */
@Repository
public interface DeductionRecordSpringRepository extends JpaRepository<DeductionRecordJpa, Long> {

  Optional<DeductionRecordJpa> findByDeductionRecordIdAndTenantId(
      UUID deductionRecordId, UUID tenantId);

  List<DeductionRecordJpa> findAllByEmployeeIdAndPeriodRefAndTenantId(
      UUID employeeId, UUID periodRef, UUID tenantId);

  List<DeductionRecordJpa> findAllByPeriodRefAndTenantId(UUID periodRef, UUID tenantId);
}
