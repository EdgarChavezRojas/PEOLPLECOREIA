package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.BankDispersionFileJpa;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankDispersionFileSpringRepository
    extends JpaRepository<BankDispersionFileJpa, UUID> {}
