package com.solveria.core.legal.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.legal.domain.event.AddendumApprovalRequiredEvent;
import com.solveria.core.legal.domain.event.AddendumSalaryAdjustmentApprovedEvent;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.legal.domain.event.ContractDraftedEvent;
import com.solveria.core.legal.domain.event.ContractLegalPisoViolatedEvent;
import com.solveria.core.legal.domain.event.ContractTacitaReconduccionRiskEvent;
import com.solveria.core.legal.domain.event.ContractTerminatedEvent;
import com.solveria.core.legal.domain.event.MaxRenewalsReachedEvent;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.legal.domain.model.vo.ComplianceSnapshot;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import com.solveria.core.legal.infrastructure.jpa.ComplianceSnapshotEmbeddable;
import com.solveria.core.legal.infrastructure.jpa.ContractAddendumJpa;
import com.solveria.core.legal.infrastructure.jpa.ContractJpa;
import com.solveria.core.legal.infrastructure.jpa.SalaryTermsEmbeddable;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ContractMapper {

  ContractJpa toJpa(Contract contract);

  @Mapping(target = "contract", ignore = true)
  @Mapping(target = "complianceSnapshot", ignore = true)
  ContractAddendumJpa toAddendumJpa(ContractAddendum addendum);

  SalaryTermsEmbeddable toEmbeddable(SalaryTerms terms);

  ComplianceSnapshotEmbeddable toEmbeddable(ComplianceSnapshot snapshot);

  default Contract toDomain(ContractJpa jpa) {
    if (jpa == null) {
      return null;
    }
    List<ContractAddendum> addendums =
        jpa.getAddendums() == null
            ? List.of()
            : jpa.getAddendums().stream().map(this::toAddendumDomain).toList();
    return Contract.rehydrate(
        jpa.getContractId(),
        jpa.getRelationshipId(),
        jpa.getContractType(),
        jpa.getEmploymentCond(),
        jpa.getStatus(),
        jpa.getProjectId(),
        (jpa.getTenantId()),
        jpa.getCreatedBy(),
        addendums,
        jpa.isTacitaReconduccionAlertSent());
  }

  default ContractAddendum toAddendumDomain(ContractAddendumJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new ContractAddendum(
        jpa.getAddendumId(),
        jpa.getStatus(),
        jpa.getEffectiveFrom(),
        jpa.getEffectiveTo(),
        toDomain(jpa.getSalaryTerms()),
        toDomain(jpa.getComplianceSnapshot()),
        jpa.getCreatedBy());
  }

  default SalaryTerms toDomain(SalaryTermsEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new SalaryTerms(
        embeddable.getBasicSalary(),
        embeddable.getTotalEarnedProj(),
        embeddable.getNetSalaryProj(),
        embeddable.getCurrency());
  }

  default ComplianceSnapshot toDomain(ComplianceSnapshotEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new ComplianceSnapshot(
        embeddable.getSmnApplied(), embeddable.getTaxRegime(), embeddable.getInfocalActive());
  }

  @AfterMapping
  default void setBackReference(@MappingTarget ContractJpa contractJpa, Contract contract) {
    if (contractJpa != null) {
      contractJpa.setTenantId(contract.getTenantId());
    }
    if (contractJpa != null && contractJpa.getAddendums() != null) {
      for (ContractAddendumJpa addendum : contractJpa.getAddendums()) {
        addendum.setContract(contractJpa);
        addendum.setTenantId(contract.getTenantId());
      }
    }
  }

  default String toEventPayload(Contract contract, DomainEvent event) {
    if (contract == null || event == null) {
      return "{}";
    }

    Map<String, Object> payload =
        Map.of(
            "contractId", contract.getContractId(),
            "relationshipId", contract.getRelationshipId(),
            "tenantId", contract.getTenantId(),
            "status", contract.getStatus() != null ? contract.getStatus().name() : null,
            "eventType", resolveEventType(event));

    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando Contract a JSON", e);
    }
  }

  default String resolveEventType(DomainEvent event) {
    if (event instanceof ContractDraftedEvent) return "CONTRACT_DRAFTED";
    if (event instanceof ContractApprovedEvent) return "CONTRACT_APPROVED";
    if (event instanceof ContractLegalPisoViolatedEvent) return "CONTRACT_LEGAL_PISO_VIOLATED";
    if (event instanceof ContractTacitaReconduccionRiskEvent)
      return "CONTRACT_TACITA_RECONDUCCION_RISK";
    if (event instanceof MaxRenewalsReachedEvent) return "MAX_RENEWALS_REACHED";
    if (event instanceof AddendumApprovalRequiredEvent) return "ADENDUM_APPROVAL_REQUIRED";
    if (event instanceof AddendumSalaryAdjustmentApprovedEvent)
      return "ADDENDUM_SALARY_ADJUSTMENT_APPROVED";
    if (event instanceof ContractTerminatedEvent) return "CONTRACT_TERMINATED";
    return event.getClass().getSimpleName();
  }
}
