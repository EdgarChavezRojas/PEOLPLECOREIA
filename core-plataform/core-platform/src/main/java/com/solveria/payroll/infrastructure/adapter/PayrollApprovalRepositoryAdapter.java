package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PayrollApprovalRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollApproval;
import com.solveria.payroll.infrastructure.jpa.PayrollApprovalJpa;
import com.solveria.payroll.infrastructure.mapper.PayrollApprovalMapper;
import com.solveria.payroll.infrastructure.repository.PayrollApprovalSpringRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PayrollApprovalRepositoryAdapter implements PayrollApprovalRepositoryPort {

    private final PayrollApprovalSpringRepository repository;
    private final PayrollApprovalMapper mapper;

    public PayrollApprovalRepositoryAdapter(PayrollApprovalSpringRepository repository, PayrollApprovalMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PayrollApproval save(PayrollApproval payrollApproval) {
        PayrollApprovalJpa jpa = mapper.toJpa(payrollApproval);
        PayrollApprovalJpa saved = repository.save(jpa);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PayrollApproval> findByRunRef(UUID runRef) {
        return repository.findByRunRef(runRef)
                .map(mapper::toDomain);
    }
}
