package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollPeriodJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA Repository: PayrollPeriod. */
@Repository
public interface PayrollPeriodSpringRepository extends JpaRepository<PayrollPeriodJpa, Long> {

    Optional<PayrollPeriodJpa> findByPeriodIdAndTenantId(UUID periodId, String tenantId);

    List<PayrollPeriodJpa> findAllByTenantId(String tenantId);

    Optional<PayrollPeriodJpa> findByMonthAndYearAndTenantId(Integer month, Integer year, String tenantId);
}
