package com.solveria.core.accruals.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.accruals.domain.event.AccrualEvent;
import com.solveria.core.accruals.domain.model.BenefitAccrual;
import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.infrastructure.jpa.BenefitAccrualJpa;
import com.solveria.core.accruals.infrastructure.jpa.HolidayCalendarJpa;
import com.solveria.core.accruals.infrastructure.jpa.QuinquenioProvisionJpa;
import java.util.Map;
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
    return HolidayCalendar.builder()
        .holidayId(jpa.getHolidayId())
        .holidayDate(jpa.getHolidayDate())
        .scope(jpa.getScope())
        .tenantId(jpa.getTenantId())
        .build();
  }

  default QuinquenioProvision toDomain(QuinquenioProvisionJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return QuinquenioProvision.builder()
        .provisionId(jpa.getProvisionId())
        .relationshipId(jpa.getRelationshipId())
        .totalAccumulated(jpa.getTotalAccumulated())
        .penaltyActive(Boolean.TRUE.equals(jpa.getPenaltyActive()))
        .tenantId(jpa.getTenantId())
        .build();
  }

  default BenefitAccrual toDomain(BenefitAccrualJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return BenefitAccrual.builder()
        .benefitId(jpa.getBenefitId())
        .relationshipId(jpa.getRelationshipId())
        .benefitType(jpa.getBenefitType())
        .fiscalYear(jpa.getFiscalYear())
        .accruedAmount(jpa.getAccruedAmount())
        .tenantId(jpa.getTenantId())
        .build();
  }

  default String toEventPayload(QuinquenioProvision provision, AccrualEvent event) {
    if (provision == null || event == null) {
      return "{}";
    }
    Map<String, Object> payload =
        Map.of(
            "provisionId", provision.getProvisionId(),
            "relationshipId", provision.getRelationshipId(),
            "tenantId", provision.getTenantId(),
            "eventType", event.type().name());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing QuinquenioProvision event payload", e);
    }
  }
}
