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

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ContractMapper {

  @Mapping(target = "tenantId", source = "tenantId")
  ContractJpa toJpa(Contract contract);

  @Mapping(target = "tenantId", source = "tenantId")
  @Mapping(target = "addendums", ignore = true)
  void updateJpa(@MappingTarget ContractJpa jpa, Contract contract);

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
    if (contractJpa == null) {
      return;
    }
    contractJpa.setTenantId(contract.getTenantId());

    if (contract.getAddendums() != null) {
      List<ContractAddendumJpa> existingJpas = contractJpa.getAddendums();
      if (existingJpas == null) {
        existingJpas = new java.util.ArrayList<>();
        contractJpa.setAddendums(existingJpas);
      }

      // Sincronizar en-lugar (merge in-place) para evitar colisiones de Hibernate con orphanRemoval
      for (ContractAddendum domainAddendum : contract.getAddendums()) {
        ContractAddendumJpa match =
            existingJpas.stream()
                .filter(a -> a.getAddendumId().equals(domainAddendum.getAddendumId()))
                .findFirst()
                .orElse(null);

        if (match != null) {
          match.setStatus(domainAddendum.getStatus());
          match.setEffectiveFrom(domainAddendum.getEffectiveFrom());
          match.setEffectiveTo(domainAddendum.getEffectiveTo());
          match.setSalaryTerms(toEmbeddable(domainAddendum.getSalaryTerms()));
          match.setComplianceSnapshot(toEmbeddable(domainAddendum.getSnapshot()));
          match.setTenantId(contract.getTenantId());
          match.setContract(contractJpa);
        } else {
          ContractAddendumJpa newJpa = toAddendumJpa(domainAddendum);
          newJpa.setContract(contractJpa);
          newJpa.setTenantId(contract.getTenantId());
          existingJpas.add(newJpa);
        }
      }

      // Eliminar huérfanos que ya no están en el modelo de dominio
      existingJpas.removeIf(
          jpaAddendum ->
              contract.getAddendums().stream()
                  .noneMatch(a -> a.getAddendumId().equals(jpaAddendum.getAddendumId())));
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
