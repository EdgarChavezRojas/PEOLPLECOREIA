package com.solveria.payroll.domain.model.entity;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.domain.model.vo.ApprovalStatus;
import java.util.UUID;

public class PayrollApproval {

  private UUID id;
  private UUID runRef;
  private ApprovalStatus status;
  private Long reviewerRef;
  private Long approverRef;
  private Boolean sodViolationFlag;
  private UUID tenantId;

  public PayrollApproval(
      UUID id,
      UUID runRef,
      ApprovalStatus status,
      Long reviewerRef,
      Long approverRef,
      Boolean sodViolationFlag,
      UUID tenantId) {
    this.id = id != null ? id : UUID.randomUUID();
    this.runRef = runRef;
    this.status = status;
    this.reviewerRef = reviewerRef;
    this.approverRef = approverRef;
    this.sodViolationFlag = sodViolationFlag;
    this.tenantId = tenantId;
  }

  public void review(Long reviewerId) {
    if (this.status != ApprovalStatus.PENDIENTE_REVISION) {
      throw new SolverExceptionImpl("INVALID_STATUS_FOR_REVIEW");
    }
    if (reviewerId == null) {
      throw new SolverExceptionImpl("REVIEWER_REQUIRED");
    }
    this.reviewerRef = reviewerId;
    this.status = ApprovalStatus.REVISADO;
  }

  public void approve(Long approverId) {
    if (this.status != ApprovalStatus.REVISADO) {
      throw new SolverExceptionImpl("INVALID_STATUS_FOR_APPROVAL");
    }
    if (approverId == null) {
      throw new SolverExceptionImpl("APPROVER_REQUIRED");
    }
    if (approverId.equals(this.reviewerRef)) {
      this.sodViolationFlag = true;
      throw new SolverExceptionImpl("PAYROLL_SOD_VIOLATION_APPROVER");
    }
    this.approverRef = approverId;
    this.status = ApprovalStatus.APROBADO;
  }

  public UUID getId() {
    return id;
  }

  public UUID getRunRef() {
    return runRef;
  }

  public ApprovalStatus getStatus() {
    return status;
  }

  public Long getReviewerRef() {
    return reviewerRef;
  }

  public Long getApproverRef() {
    return approverRef;
  }

  public Boolean getSodViolationFlag() {
    return sodViolationFlag;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}
