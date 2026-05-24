package com.solveria.payroll.domain.model.entity;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.domain.model.vo.ApprovalStatus;
import java.util.UUID;

public class PayrollApproval {

  private UUID id;
  private UUID runRef;
  private ApprovalStatus status;
  private UUID creatorRef;
  private UUID reviewerRef;
  private UUID approverRef;
  private Boolean sodViolationFlag;
  private UUID tenantId;

  public PayrollApproval(
      UUID id,
      UUID runRef,
      ApprovalStatus status,
      UUID creatorRef,
      UUID reviewerRef,
      UUID approverRef,
      Boolean sodViolationFlag,
      UUID tenantId) {
    this.id = id;
    this.runRef = runRef;
    this.status = status;
    this.creatorRef = creatorRef;
    this.reviewerRef = reviewerRef;
    this.approverRef = approverRef;
    this.sodViolationFlag = sodViolationFlag;
    this.tenantId = tenantId;
  }

  public static PayrollApproval createDraft(UUID id, UUID runRef, UUID creatorRef, UUID tenantId) {
    return new PayrollApproval(
        id, runRef, ApprovalStatus.PENDIENTE_REVISION, creatorRef, null, null, false, tenantId);
  }

  public void review(UUID reviewerId) {
    if (reviewerId.equals(this.creatorRef)) {
      this.sodViolationFlag = true;
      throw new SolverExceptionImpl("PAYROLL_SOD_VIOLATION_REVIEWER");
    }
    this.reviewerRef = reviewerId;
    this.status = ApprovalStatus.REVISADO;
  }

  public void approve(UUID approverId) {
    if (this.status != ApprovalStatus.REVISADO) {
      throw new SolverExceptionImpl("INVALID_STATUS_FOR_APPROVAL");
    }
    if (approverId.equals(this.creatorRef) || approverId.equals(this.reviewerRef)) {
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

  public UUID getCreatorRef() {
    return creatorRef;
  }

  public UUID getReviewerRef() {
    return reviewerRef;
  }

  public UUID getApproverRef() {
    return approverRef;
  }

  public Boolean getSodViolationFlag() {
    return sodViolationFlag;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}
