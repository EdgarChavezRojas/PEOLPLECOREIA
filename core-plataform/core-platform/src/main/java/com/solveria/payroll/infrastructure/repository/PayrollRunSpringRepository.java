package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollRunJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRunSpringRepository extends JpaRepository<PayrollRunJpa, UUID> {

  @Query("SELECT r FROM PayrollRunJpa r LEFT JOIN FETCH r.lines WHERE r.payrollRunId = :id")
  Optional<PayrollRunJpa> findByIdWithLines(@Param("id") UUID id);

  @Query(
      "SELECT r FROM PayrollRunJpa r LEFT JOIN FETCH r.lines WHERE r.periodRef = :periodRef AND r.tenantId = :tenantId")
  List<PayrollRunJpa> findByPeriodAndTenant(
      @Param("periodRef") UUID periodRef, @Param("tenantId") UUID tenantId);

  @Query("SELECT r FROM PayrollRunJpa r WHERE r.tenantId = :tenantId ORDER BY r.createdAt DESC")
  List<PayrollRunJpa> findAllByTenantId(@Param("tenantId") UUID tenantId);
}
