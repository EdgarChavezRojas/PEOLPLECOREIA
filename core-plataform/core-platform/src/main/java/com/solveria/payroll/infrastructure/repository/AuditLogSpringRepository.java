package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.AuditLogJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogSpringRepository extends JpaRepository<AuditLogJpa, UUID> {
}
