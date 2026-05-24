package com.solveria.core.accruals.application.port;

import com.solveria.core.accruals.domain.model.BenefitAccrual;
import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.model.vo.BenefitType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenefitsRepositoryPort {

  HolidayCalendar saveHoliday(HolidayCalendar holiday);

  List<HolidayCalendar> findHolidaysBetween(LocalDate startDate, LocalDate endDate);

  QuinquenioProvision saveQuinquenio(QuinquenioProvision provision);

  Optional<QuinquenioProvision> findQuinquenioByRelationshipId(UUID relationshipId);

  List<QuinquenioProvision> findAllQuinquenioProvisions();

  BenefitAccrual saveBenefitAccrual(BenefitAccrual accrual);

  List<BenefitAccrual> saveBenefitAccrualBatch(List<BenefitAccrual> accruals);

  Optional<BenefitAccrual> findBenefitAccrual(
      UUID relationshipId, BenefitType benefitType, int fiscalYear);

  List<BenefitAccrual> findAllBenefitAccruals();
}
