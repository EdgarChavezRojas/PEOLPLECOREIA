package com.solveria.core.accruals.infrastructure.repository;

import com.solveria.core.accruals.domain.model.vo.BenefitType;
import com.solveria.core.accruals.infrastructure.jpa.BenefitAccrualJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitAccrualRepository extends JpaRepository<BenefitAccrualJpa, UUID> {

  Optional<BenefitAccrualJpa> findByBenefitTypeAndFiscalYearAndTenantId(
      BenefitType benefitType, int fiscalYear, UUID tenantId);
}
