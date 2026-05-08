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


    @Column(name = "run_ref", nullable = false)
    private UUID runRef;

    @Column(name = "user_ref", nullable = false)
    private UUID userRef;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "details")
    private String details;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;



    public UUID getRunRef() { return runRef; }
    public void setRunRef(UUID runRef) { this.runRef = runRef; }
    public UUID getUserRef() { return userRef; }
    public void setUserRef(UUID userRef) { this.userRef = userRef; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
