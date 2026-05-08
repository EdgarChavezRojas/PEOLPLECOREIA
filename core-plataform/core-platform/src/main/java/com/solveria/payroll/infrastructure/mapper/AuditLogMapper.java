package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.AuditLog;
import com.solveria.payroll.infrastructure.jpa.AuditLogJpa;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {
    AuditLogJpa toJpa(AuditLog domain);
    AuditLog toDomain(AuditLogJpa jpa);
}
