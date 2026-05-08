package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollApprovalJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollApprovalSpringRepository extends JpaRepository<PayrollApprovalJpa, UUID> {
    Optional<PayrollApprovalJpa> findByRunRef(UUID runRef);
}
