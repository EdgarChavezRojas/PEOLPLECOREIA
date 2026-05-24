package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollGroupJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: PayrollGroup. */
@Repository
public interface PayrollGroupSpringRepository extends JpaRepository<PayrollGroupJpa, Long> {

  Optional<PayrollGroupJpa> findByGroupIdAndTenantId(UUID groupId, UUID tenantId);

  List<PayrollGroupJpa> findAllByTenantId(UUID tenantId);

  Optional<PayrollGroupJpa> findByGroupCodeAndTenantId(String groupCode, UUID tenantId);
}
