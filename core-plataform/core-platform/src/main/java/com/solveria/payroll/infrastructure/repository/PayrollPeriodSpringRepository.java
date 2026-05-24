package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollPeriodJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: PayrollPeriod. */
@Repository
public interface PayrollPeriodSpringRepository extends JpaRepository<PayrollPeriodJpa, Long> {

  Optional<PayrollPeriodJpa> findByPeriodIdAndTenantId(UUID periodId, UUID tenantId);

  List<PayrollPeriodJpa> findAllByTenantId(UUID tenantId);

  Optional<PayrollPeriodJpa> findByMonthAndYearAndTenantId(
      Integer month, Integer year, UUID tenantId);
}
