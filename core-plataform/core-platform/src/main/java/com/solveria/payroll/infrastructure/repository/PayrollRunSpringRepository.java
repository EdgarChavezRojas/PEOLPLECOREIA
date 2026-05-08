package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PayrollRunJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayrollRunSpringRepository extends JpaRepository<PayrollRunJpa, UUID> {
}
