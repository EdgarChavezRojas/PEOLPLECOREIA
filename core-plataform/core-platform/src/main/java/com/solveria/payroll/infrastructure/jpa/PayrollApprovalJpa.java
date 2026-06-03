package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "prl_payroll_approval")
public class PayrollApprovalJpa extends BaseEntity {
  @Id
  @Column(name = "payroll_approval_id", updatable = false, columnDefinition = "UUID")
  private UUID payrollApprovalId;

  @Column(name = "run_ref", nullable = false)
  private UUID runRef;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "reviewer_ref")
  private Long reviewerRef;

  @Column(name = "approver_ref")
  private Long approverRef;

  @Column(name = "sod_violation_flag", nullable = false)
  private Boolean sodViolationFlag;

  @Column(name = "tenant_id")
  private UUID tenantId;

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public UUID getPayrollApprovalId() {
    return payrollApprovalId;
  }

  public void setPayrollApprovalId(UUID payrollApprovalId) {
    this.payrollApprovalId = payrollApprovalId;
  }

  public UUID getRunRef() {
    return runRef;
  }

  public void setRunRef(UUID runRef) {
    this.runRef = runRef;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Long getReviewerRef() {
    return reviewerRef;
  }

  public void setReviewerRef(Long reviewerRef) {
    this.reviewerRef = reviewerRef;
  }

  public Long getApproverRef() {
    return approverRef;
  }

  public void setApproverRef(Long approverRef) {
    this.approverRef = approverRef;
  }

  public Boolean getSodViolationFlag() {
    return sodViolationFlag;
  }

  public void setSodViolationFlag(Boolean sodViolationFlag) {
    this.sodViolationFlag = sodViolationFlag;
  }
}
