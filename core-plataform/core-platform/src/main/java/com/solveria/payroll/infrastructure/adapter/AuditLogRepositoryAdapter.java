package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.AuditLogRepositoryPort;
import com.solveria.payroll.domain.model.entity.AuditLog;
import com.solveria.payroll.infrastructure.jpa.AuditLogJpa;
import com.solveria.payroll.infrastructure.mapper.AuditLogMapper;
import com.solveria.payroll.infrastructure.repository.AuditLogSpringRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final AuditLogSpringRepository repository;
    private final AuditLogMapper mapper;

    public AuditLogRepositoryAdapter(AuditLogSpringRepository repository, AuditLogMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpa jpa = mapper.toJpa(auditLog);
        AuditLogJpa saved = repository.save(jpa);
        return mapper.toDomain(saved);
    }
}
