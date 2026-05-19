package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "prl_audit_log")
public class AuditLogJpa extends BaseEntity {

    @Column(name = "audit_log_id", updatable = false, columnDefinition = "UUID")
    private UUID auditLogId;
    @Column(name = "run_ref", nullable = false)
    private UUID runRef;

    @Column(name = "user_ref", nullable = false)
    private UUID userRef;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "details")
    private String details;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;


    public UUID getAuditLogId() { return auditLogId; }
    public void setAuditLogId(UUID auditLogId) { this.auditLogId = auditLogId; }
    public UUID getRunRef() { return runRef; }
    public void setRunRef(UUID runRef) { this.runRef = runRef; }
    public UUID getUserRef() { return userRef; }
    public void setUserRef(UUID userRef) { this.userRef = userRef; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
}
