package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollClosureJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollClosureSpringRepository extends JpaRepository<PayrollClosureJpa, UUID> {
  Optional<PayrollClosureJpa> findByRunRef(UUID runRef);
}
