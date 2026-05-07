package com.solveria.core.financial.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.financial.domain.event.CostCenterSplitAdjustedEvent;
import com.solveria.core.financial.domain.event.FundingSourceProjectExhaustedEvent;
import com.solveria.core.financial.domain.event.FundingSourceValidatedEvent;
import com.solveria.core.financial.domain.model.FundingSource;
import com.solveria.core.financial.domain.model.vo.LaborCostSplit;
import com.solveria.core.financial.infrastructure.jpa.FundingSourceJpa;
import com.solveria.core.financial.infrastructure.jpa.LaborCostSplitJpa;
import com.solveria.core.shared.events.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;

/** MapStruct Mapper: FundingSource Domain ↔ JPA. */
@Mapper(componentModel = "spring")
public interface FundingSourceMapper {

  default FundingSourceJpa toJpa(FundingSource source) {
    if (source == null) {
      return null;
    }
    FundingSourceJpa jpa = new FundingSourceJpa();
    jpa.setSourceId(source.getSourceId());
    jpa.setProjectCode(source.getProjectCode());
    jpa.setTotalBudget(source.getTotalBudget());
    jpa.setAvailableBudget(source.getAvailableBudget());
    jpa.setBurnRate(source.getBurnRate());
    jpa.setTenantId(source.getTenantId());
    jpa.setCreatedByUser(source.getCreatedBy());

    List<LaborCostSplitJpa> splitJpas = new ArrayList<>();
    if (source.getCostSplits() != null) {
      for (LaborCostSplit split : source.getCostSplits()) {
        LaborCostSplitJpa splitJpa = new LaborCostSplitJpa();
        splitJpa.setSplitId(split.splitId());
        splitJpa.setUnitId(split.unitId());
        splitJpa.setPercentage(split.percentage());
        splitJpa.setEffectiveDate(split.effectiveDate());
        splitJpa.setFundingSource(jpa);
        splitJpa.setTenantId(source.getTenantId());
        splitJpas.add(splitJpa);
      }
    }
    jpa.setCostSplits(splitJpas);
    return jpa;
  }

  default FundingSource toDomain(FundingSourceJpa jpa) {
    if (jpa == null) {
      return null;
    }
    List<LaborCostSplit> splits =
        jpa.getCostSplits() == null
            ? List.of()
            : jpa.getCostSplits().stream()
                .map(
                    s ->
                        new LaborCostSplit(
                            s.getSplitId(), s.getUnitId(), s.getPercentage(), s.getEffectiveDate()))
                .toList();

    return FundingSource.rehydrate(
        jpa.getSourceId(),
        jpa.getProjectCode(),
        jpa.getTotalBudget(),
        jpa.getAvailableBudget(),
        jpa.getBurnRate(),
        jpa.getTenantId(),
        jpa.getCreatedByUser(),
        splits);
  }

  default String toEventPayload(FundingSource source, DomainEvent event) {
    if (source == null || event == null) {
      return "{}";
    }
    Map<String, Object> payload =
        Map.of(
            "sourceId", source.getSourceId(),
            "projectCode", source.getProjectCode(),
            "tenantId", source.getTenantId(),
            "availableBudget", source.getAvailableBudget(),
            "eventType", resolveEventType(event));
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando FundingSource a JSON", e);
    }
  }

  default String resolveEventType(DomainEvent event) {
    if (event instanceof FundingSourceValidatedEvent) return "FUNDING_SOURCE_VALIDATED";
    if (event instanceof FundingSourceProjectExhaustedEvent)
      return "FINANCIAL_SOURCE_PROJECT_EXHAUSTED";
    if (event instanceof CostCenterSplitAdjustedEvent) return "COST_CENTER_SPLIT_ADJUSTED";
    return event.getClass().getSimpleName();
  }
}
