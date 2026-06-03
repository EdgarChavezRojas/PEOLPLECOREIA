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
import com.solveria.core.legal.domain.model.vo.AddendumStatus;
import com.solveria.core.legal.domain.model.vo.ComplianceSnapshot;
import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Contract extends DomainRoot {

  private static final int MAX_RENEWALS = 2;

  private final UUID contractId;
  private final UUID relationshipId;
  private final ContractType contractType;
  private final EmploymentCondition employmentCond;
  private ContractStatus status;
  private final UUID projectId;
  private final UUID tenantId;
  private final String createdBy;
  private final List<ContractAddendum> addendums;
  private boolean tacitaReconduccionAlertSent;

  public UUID getContractId() {
    return contractId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public ContractType getContractType() {
    return contractType;
  }

  public EmploymentCondition getEmploymentCond() {
    return employmentCond;
  }

  public ContractStatus getStatus() {
    return status;
  }

  public UUID getProjectId() {
    return projectId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public List<ContractAddendum> getAddendums() {
    return addendums;
  }

  public boolean isTacitaReconduccionAlertSent() {
    return tacitaReconduccionAlertSent;
  }

  private Contract(
      UUID contractId,
      UUID relationshipId,
      ContractType contractType,
      EmploymentCondition employmentCond,
      ContractStatus status,
      UUID projectId,
      UUID tenantId,
      String createdBy,
      List<ContractAddendum> addendums,
      boolean tacitaReconduccionAlertSent) {
    this.contractId = Objects.requireNonNull(contractId, "contractId");
    this.relationshipId = Objects.requireNonNull(relationshipId, "relationshipId");
    this.contractType = Objects.requireNonNull(contractType, "contractType");
    this.employmentCond = employmentCond;
    this.status = Objects.requireNonNull(status, "status");
    this.projectId = projectId;
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
    this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
    this.addendums = new ArrayList<>(Objects.requireNonNullElseGet(addendums, List::of));
    this.tacitaReconduccionAlertSent = tacitaReconduccionAlertSent;
  }

  public static Contract draft(
      UUID contractId,
      UUID relationshipId,
      ContractType contractType,
      EmploymentCondition employmentCond,
      UUID projectId,
      UUID tenantId,
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
            List.of(),
            false);
    contract.registerEvent(new ContractDraftedEvent(contractId, relationshipId, Instant.now()));
    return contract;
  }

  public static Contract rehydrate(
      UUID contractId,
      UUID relationshipId,
      ContractType contractType,
      EmploymentCondition employmentCond,
      ContractStatus status,
      UUID projectId,
      UUID tenantId,
      String createdBy,
      List<ContractAddendum> addendums,
      boolean tacitaReconduccionAlertSent) {
    return new Contract(
        contractId,
        relationshipId,
        contractType,
        employmentCond,
        status,
        projectId,
        tenantId,
        createdBy,
        addendums,
        tacitaReconduccionAlertSent);
  }

  public void approve(String createdBy, String approvedBy) {

    validateSegregationOfDuties(createdBy, approvedBy);
    if (status != ContractStatus.DRAFT) {
      throw new InvalidContractStatusException(status, ContractStatus.DRAFT);
    }
    if (addendums == null || addendums.isEmpty()) {
      throw new IllegalStateException(
          "No se puede aprobar un contrato sin al menos una adenda propuesta");
    }

    // Al aprobar el contrato, se aprueban automáticamente todas sus adendas iniciales propuestas
    for (ContractAddendum addendum : addendums) {
      if (addendum.getStatus() == AddendumStatus.PENDING_APPROVAL) {
        addendum.approve(approvedBy);
        registerEvent(
            new AddendumSalaryAdjustmentApprovedEvent(
                contractId, addendum.getAddendumId(), Instant.now()));
      }
    }

    status = ContractStatus.APPROVED;
    registerEvent(new ContractApprovedEvent(contractId, tenantId));
  }

  public ContractAddendum proposeAddendum(
      UUID addendumId,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      SalaryTerms salaryTerms,
      ComplianceSnapshot snapshot,
      String createdBy,
      BigDecimal salaryFloor) {

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
    registerEvent(new AddendumApprovalRequiredEvent(contractId, addendumId, Instant.now()));
    return addendum;
  }

  public void approveAddendum(UUID addendumId, String createdBy, String approvedBy) {

    validateSegregationOfDuties(createdBy, approvedBy);
    ContractAddendum addendum = findAddendum(addendumId);
    addendum.approve(approvedBy);
    registerEvent(new AddendumSalaryAdjustmentApprovedEvent(contractId, addendumId, Instant.now()));
  }

  public void terminate(String createdBy, String approvedBy) {

    validateSegregationOfDuties(createdBy, approvedBy);
    status = ContractStatus.TERMINATED;
    registerEvent(new ContractTerminatedEvent(contractId, tenantId, Instant.now()));
  }

  public void markTacitaReconduccionRisk() {

    registerEvent(new ContractTacitaReconduccionRiskEvent(contractId, Instant.now()));
  }

  public void markTacitaReconduccionAlertSent() {

    this.tacitaReconduccionAlertSent = true;
  }

  public void validateRenewalLimit(int renewalCount) {

    if (renewalCount >= MAX_RENEWALS) {
      registerEvent(new MaxRenewalsReachedEvent(contractId, renewalCount, Instant.now()));
      throw new MaxRenewalsReachedException(renewalCount, MAX_RENEWALS);
    }
  }

  private ContractAddendum findAddendum(UUID addendumId) {
    return addendums.stream()
        .filter(addendum -> addendum.getAddendumId().equals(addendumId))
        .findFirst()
        .orElseThrow(() -> new AddendumNotFoundException(addendumId));
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
      registerEvent(
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
