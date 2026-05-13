package com.solveria.core.accruals.infrastructure.repository;

import com.solveria.core.accruals.domain.model.vo.BenefitType;
import com.solveria.core.accruals.infrastructure.jpa.BenefitAccrualJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitAccrualRepository extends JpaRepository<BenefitAccrualJpa, UUID> {

  Optional<BenefitAccrualJpa> findByRelationshipIdAndBenefitTypeAndFiscalYear(
      UUID relationshipId, BenefitType benefitType, int fiscalYear);

  Optional<BenefitAccrualJpa> findByRelationshipIdAndBenefitTypeAndFiscalYearAndTenantId(
      UUID relationshipId, BenefitType benefitType, int fiscalYear, UUID tenantId);
}
