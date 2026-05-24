package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollApprovalJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollApprovalSpringRepository extends JpaRepository<PayrollApprovalJpa, UUID> {
  Optional<PayrollApprovalJpa> findByRunRef(UUID runRef);
}
