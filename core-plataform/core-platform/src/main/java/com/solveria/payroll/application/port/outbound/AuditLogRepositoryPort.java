package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.entity.AuditLog;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog auditLog);
}
