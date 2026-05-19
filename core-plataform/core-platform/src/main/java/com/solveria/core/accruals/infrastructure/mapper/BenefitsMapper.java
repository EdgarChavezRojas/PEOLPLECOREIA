package com.solveria.core.accruals.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.accruals.domain.model.BenefitAccrual;
import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.infrastructure.jpa.BenefitAccrualJpa;
import com.solveria.core.accruals.infrastructure.jpa.HolidayCalendarJpa;
import com.solveria.core.accruals.infrastructure.jpa.QuinquenioProvisionJpa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BenefitsMapper {

  HolidayCalendarJpa toJpa(HolidayCalendar holiday);

  QuinquenioProvisionJpa toJpa(QuinquenioProvision provision);

  BenefitAccrualJpa toJpa(BenefitAccrual accrual);

  default HolidayCalendar toDomain(HolidayCalendarJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new HolidayCalendar(
            jpa.getHolidayId(),
            jpa.getHolidayDate(),
            jpa.getScope(),
            jpa.getTenantId()
    );
  }

  default QuinquenioProvision toDomain(QuinquenioProvisionJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new QuinquenioProvision(
            jpa.getProvisionId(),
            jpa.getRelationshipId(),
            jpa.getTotalAccumulated(),
            Boolean.TRUE.equals(jpa.getPenaltyActive()),
            jpa.getTenantId()
    );
  }

  default BenefitAccrual toDomain(BenefitAccrualJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new BenefitAccrual(
            jpa.getBenefitId(),
            jpa.getRelationshipId(),
            jpa.getBenefitType(),
            jpa.getFiscalYear(),
            jpa.getAccruedAmount(),
            jpa.getTenantId()
    );
  }

  default String toEventPayload(QuinquenioProvision provision, DomainEvent event) {
    if (provision == null || event == null) {
      return "{}";
    }
    try {
      return new ObjectMapper().writeValueAsString(event);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing QuinquenioProvision event payload", e);
    }
  }
}
