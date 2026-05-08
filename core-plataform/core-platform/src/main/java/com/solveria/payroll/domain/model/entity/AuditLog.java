package com.solveria.payroll.domain.model.entity;

import com.solveria.payroll.domain.model.vo.AuditAction;
import java.util.UUID;

public class AuditLog {

    private final UUID id;
    private final UUID runRef;
    private final UUID userRef;
    private final AuditAction action;
    private final String details;
    private final String tenantId;

    public AuditLog(UUID id, UUID runRef, UUID userRef, AuditAction action, String details, String tenantId) {
        this.id = id;
        this.runRef = runRef;
        this.userRef = userRef;
        this.action = action;
        this.details = details;
        this.tenantId = tenantId;
    }

    public UUID getId() { return id; }
    public UUID getRunRef() { return runRef; }
    public UUID getUserRef() { return userRef; }
    public AuditAction getAction() { return action; }
    public String getDetails() { return details; }
    public String getTenantId() { return tenantId; }
}
