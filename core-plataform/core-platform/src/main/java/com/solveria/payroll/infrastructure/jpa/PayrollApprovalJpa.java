package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "prl_payroll_approval")
public class PayrollApprovalJpa extends BaseEntity {
    @Column(name = "payroll_approval_id", updatable = false, columnDefinition = "UUID")
    private UUID payrollApprovalId;
    @Column(name = "run_ref", nullable = false)
    private UUID runRef;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "creator_ref", nullable = false)
    private UUID creatorRef;

    @Column(name = "reviewer_ref")
    private UUID reviewerRef;

    @Column(name = "approver_ref")
    private UUID approverRef;

    @Column(name = "sod_violation_flag", nullable = false)
    private Boolean sodViolationFlag;
    public UUID getPayrollApprovalId() { return payrollApprovalId; }
    public void setPayrollApprovalId(UUID payrollApprovalId) { this.payrollApprovalId = payrollApprovalId; }
    public UUID getRunRef() { return runRef; }
    public void setRunRef(UUID runRef) { this.runRef = runRef; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getCreatorRef() { return creatorRef; }
    public void setCreatorRef(UUID creatorRef) { this.creatorRef = creatorRef; }
    public UUID getReviewerRef() { return reviewerRef; }
    public void setReviewerRef(UUID reviewerRef) { this.reviewerRef = reviewerRef; }
    public UUID getApproverRef() { return approverRef; }
    public void setApproverRef(UUID approverRef) { this.approverRef = approverRef; }
    public Boolean getSodViolationFlag() { return sodViolationFlag; }
    public void setSodViolationFlag(Boolean sodViolationFlag) { this.sodViolationFlag = sodViolationFlag; }
}
