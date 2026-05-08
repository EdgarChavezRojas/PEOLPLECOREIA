package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.BankDispersionFileJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BankDispersionFileSpringRepository extends JpaRepository<BankDispersionFileJpa, UUID> {
}
