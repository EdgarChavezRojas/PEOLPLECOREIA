package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.AuditLog;
import com.solveria.payroll.infrastructure.jpa.AuditLogJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {
    @Mapping(target = "id", ignore = true) // Ignores the Long id from BaseEntity
    @Mapping(target = "auditLogId", source = "id")
    AuditLogJpa toJpa(AuditLog domain);
    @Mapping(target = "id", source = "auditLogId")
    AuditLog toDomain(AuditLogJpa jpa);
}
