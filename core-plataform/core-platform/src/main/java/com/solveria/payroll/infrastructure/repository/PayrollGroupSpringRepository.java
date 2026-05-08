package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollGroupJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA Repository: PayrollGroup. */
@Repository
public interface PayrollGroupSpringRepository extends JpaRepository<PayrollGroupJpa, Long> {

    Optional<PayrollGroupJpa> findByGroupIdAndTenantId(UUID groupId, String tenantId);

    List<PayrollGroupJpa> findAllByTenantId(String tenantId);

    Optional<PayrollGroupJpa> findByGroupCodeAndTenantId(String groupCode, String tenantId);
}
