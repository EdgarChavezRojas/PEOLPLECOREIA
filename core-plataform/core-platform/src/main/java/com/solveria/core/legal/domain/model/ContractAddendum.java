package com.solveria.core.legal.domain.model;

import com.solveria.core.legal.domain.exception.SegregationOfDutiesViolationException;
import com.solveria.core.legal.domain.model.vo.AddendumStatus;
import com.solveria.core.legal.domain.model.vo.ComplianceSnapshot;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;


public class ContractAddendum {

  private final UUID addendumId;
  private AddendumStatus status;
  private final LocalDate effectiveFrom;
  private final LocalDate effectiveTo;
  private final SalaryTerms salaryTerms;
  private final ComplianceSnapshot snapshot;
  private final String createdBy;

  public ContractAddendum(
      UUID addendumId,
      AddendumStatus status,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      SalaryTerms salaryTerms,
      ComplianceSnapshot snapshot,
      String createdBy) {
    this.addendumId = Objects.requireNonNull(addendumId, "addendumId");
    this.status = Objects.requireNonNull(status, "status");
    this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom");
    this.effectiveTo = Objects.requireNonNull(effectiveTo, "effectiveTo");
    this.salaryTerms = Objects.requireNonNull(salaryTerms, "salaryTerms");
    this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
  }

      public UUID getAddendumId() {
          return addendumId;
      }

      public AddendumStatus getStatus() {
          return status;
      }

      public LocalDate getEffectiveFrom() {
          return effectiveFrom;
      }

      public LocalDate getEffectiveTo() {
          return effectiveTo;
      }

      public SalaryTerms getSalaryTerms() {
          return salaryTerms;
      }

      public ComplianceSnapshot getSnapshot() {
          return snapshot;
      }

      public String getCreatedBy() {
          return createdBy;
      }

  public void approve(String approvedBy) {
    validateSegregationOfDuties(createdBy, approvedBy);
    this.status = AddendumStatus.APPROVED;
  }

  private void validateSegregationOfDuties(String creator, String approver) {
    if (Objects.equals(creator, approver)) {
      throw new SegregationOfDutiesViolationException(creator, approver);
    }
  }
}
