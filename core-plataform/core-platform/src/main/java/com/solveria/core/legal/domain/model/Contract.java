package com.solveria.core.legal.domain.model;

import com.solveria.core.legal.domain.event.AddendumApprovalRequiredEvent;
import com.solveria.core.legal.domain.event.AddendumSalaryAdjustmentApprovedEvent;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.legal.domain.event.ContractDraftedEvent;
import com.solveria.core.legal.domain.event.ContractLegalPisoViolatedEvent;
import com.solveria.core.legal.domain.event.ContractTacitaReconduccionRiskEvent;
import com.solveria.core.legal.domain.event.ContractTerminatedEvent;
import com.solveria.core.legal.domain.event.MaxRenewalsReachedEvent;
import com.solveria.core.legal.domain.exception.AddendumNotFoundException;
import com.solveria.core.legal.domain.exception.ContractLegalPisoViolatedException;
import com.solveria.core.legal.domain.exception.EffectiveDatingOverlapException;
import com.solveria.core.legal.domain.exception.InvalidContractStatusException;
import com.solveria.core.legal.domain.exception.MaxRenewalsReachedException;
import com.solveria.core.legal.domain.exception.SegregationOfDutiesViolationException;
import com.solveria.core.legal.domain.exception.TenantIsolationViolationException;
import com.solveria.core.legal.domain.model.vo.AddendumStatus;
import com.solveria.core.legal.domain.model.vo.ComplianceSnapshot;
import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Contract {

  private static final int MAX_RENEWALS = 2;

  private final UUID contractId;
  private final UUID relationshipId;
  private final ContractType contractType;
  private final EmploymentCondition employmentCond;
  private ContractStatus status;
  private final String projectId;
  private final String tenantId;
  private final String createdBy;
  private final List<ContractAddendum> addendums;
  private final List<DomainEvent> domainEvents = new ArrayList<>();

  private Contract(
      UUID contractId,
      UUID relationshipId,
      ContractType contractType,
      EmploymentCondition employmentCond,
      ContractStatus status,
      String projectId,
      String tenantId,
      String createdBy,
      List<ContractAddendum> addendums) {
    this.contractId = Objects.requireNonNull(contractId, "contractId");
    this.relationshipId = Objects.requireNonNull(relationshipId, "relationshipId");
    this.contractType = Objects.requireNonNull(contractType, "contractType");
    this.employmentCond = employmentCond;
    this.status = Objects.requireNonNull(status, "status");
    this.projectId = projectId;
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
    this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
    this.addendums = new ArrayList<>(Objects.requireNonNullElseGet(addendums, List::of));
    validateTenant();
  }

  public static Contract draft(
      UUID contractId,
      UUID relationshipId,
      ContractType contractType,
      EmploymentCondition employmentCond,
      String projectId,
      String tenantId,
      String createdBy) {
    Contract contract =
        new Contract(
            contractId,
            relationshipId,
            contractType,
            employmentCond,
            ContractStatus.DRAFT,
            projectId,
            tenantId,
            createdBy,
            List.of());
    contract.domainEvents.add(new ContractDraftedEvent(contractId, relationshipId, Instant.now()));
    return contract;
  }

  public static Contract rehydrate(
      UUID contractId,
      UUID relationshipId,
      ContractType contractType,
      EmploymentCondition employmentCond,
      ContractStatus status,
      String projectId,
      String tenantId,
      String createdBy,
      List<ContractAddendum> addendums) {
    return new Contract(
        contractId,
        relationshipId,
        contractType,
        employmentCond,
        status,
        projectId,
        tenantId,
        createdBy,
        addendums);
  }

  public void approve(String createdBy, String approvedBy) {
    validateTenant();
    validateSegregationOfDuties(createdBy, approvedBy);
    if (status != ContractStatus.DRAFT) {
      throw new InvalidContractStatusException(status, ContractStatus.DRAFT);
    }
    status = ContractStatus.APPROVED;
    domainEvents.add(new ContractApprovedEvent(contractId, Instant.now()));
  }

  public ContractAddendum proposeAddendum(
      UUID addendumId,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      SalaryTerms salaryTerms,
      ComplianceSnapshot snapshot,
      String createdBy,
      BigDecimal salaryFloor) {
    validateTenant();
    if (ContractType.PLAZO_FIJO.equals(contractType)) {
      validateRenewalLimit(addendums.size());
    }
    validateSalaryFloor(salaryTerms, salaryFloor);
    validateEffectiveDating(effectiveFrom, effectiveTo);
    ContractAddendum addendum =
        new ContractAddendum(
            addendumId,
            AddendumStatus.PENDING_APPROVAL,
            effectiveFrom,
            effectiveTo,
            salaryTerms,
            snapshot,
            createdBy);
    addendums.add(addendum);
    domainEvents.add(new AddendumApprovalRequiredEvent(contractId, addendumId, Instant.now()));
    return addendum;
  }

  public void approveAddendum(UUID addendumId, String createdBy, String approvedBy) {
    validateTenant();
    validateSegregationOfDuties(createdBy, approvedBy);
    ContractAddendum addendum = findAddendum(addendumId);
    addendum.approve(approvedBy);
    domainEvents.add(
        new AddendumSalaryAdjustmentApprovedEvent(contractId, addendumId, Instant.now()));
  }

  public void terminate(String createdBy, String approvedBy) {
    validateTenant();
    validateSegregationOfDuties(createdBy, approvedBy);
    status = ContractStatus.TERMINATED;
    domainEvents.add(new ContractTerminatedEvent(contractId, Instant.now()));
  }

  public void markTacitaReconduccionRisk() {
    validateTenant();
    domainEvents.add(new ContractTacitaReconduccionRiskEvent(contractId, Instant.now()));
  }

  public void validateRenewalLimit(int renewalCount) {
    validateTenant();
    if (renewalCount >= MAX_RENEWALS) {
      domainEvents.add(new MaxRenewalsReachedEvent(contractId, renewalCount, Instant.now()));
      throw new MaxRenewalsReachedException(renewalCount, MAX_RENEWALS);
    }
  }

  public List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }

  private ContractAddendum findAddendum(UUID addendumId) {
    return addendums.stream()
        .filter(addendum -> addendum.getAddendumId().equals(addendumId))
        .findFirst()
        .orElseThrow(() -> new AddendumNotFoundException(addendumId));
  }

  private void validateTenant() {
    String currentTenantId = SecurityTenantContext.getCurrentTenantId();
    if (!Objects.equals(tenantId, currentTenantId)) {
      throw new TenantIsolationViolationException(tenantId, currentTenantId);
    }
  }

  private void validateSegregationOfDuties(String creator, String approver) {
    if (Objects.equals(creator, approver)) {
      throw new SegregationOfDutiesViolationException(creator, approver);
    }
  }

  private void validateSalaryFloor(SalaryTerms salaryTerms, BigDecimal floor) {
    if (salaryTerms == null || salaryTerms.basicSalary() == null || floor == null) {
      return;
    }
    if (salaryTerms.basicSalary().compareTo(floor) < 0) {
      domainEvents.add(
          new ContractLegalPisoViolatedEvent(contractId, salaryTerms.basicSalary(), Instant.now()));
      throw new ContractLegalPisoViolatedException(salaryTerms.basicSalary(), floor);
    }
  }

  private void validateEffectiveDating(LocalDate effectiveFrom, LocalDate effectiveTo) {
    for (ContractAddendum existing : addendums) {
      if (overlaps(
          existing.getEffectiveFrom(), existing.getEffectiveTo(), effectiveFrom, effectiveTo)) {
        throw new EffectiveDatingOverlapException(
            existing.getAddendumId(), effectiveFrom, effectiveTo);
      }
    }
  }

  private boolean overlaps(
      LocalDate existingFrom, LocalDate existingTo, LocalDate newFrom, LocalDate newTo) {
    LocalDate resolvedExistingTo = existingTo != null ? existingTo : LocalDate.MAX;
    LocalDate resolvedNewTo = newTo != null ? newTo : LocalDate.MAX;
    return !newFrom.isAfter(resolvedExistingTo) && !resolvedNewTo.isBefore(existingFrom);
  }
}
