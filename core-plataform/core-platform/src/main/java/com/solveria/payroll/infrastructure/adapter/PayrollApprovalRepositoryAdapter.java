package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PayrollApprovalRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollApproval;
import com.solveria.payroll.infrastructure.jpa.PayrollApprovalJpa;
import com.solveria.payroll.infrastructure.mapper.PayrollApprovalMapper;
import com.solveria.payroll.infrastructure.repository.PayrollApprovalSpringRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PayrollApprovalRepositoryAdapter implements PayrollApprovalRepositoryPort {

  private final PayrollApprovalSpringRepository repository;
  private final PayrollApprovalMapper mapper;

  public PayrollApprovalRepositoryAdapter(
      PayrollApprovalSpringRepository repository, PayrollApprovalMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public PayrollApproval save(PayrollApproval payrollApproval) {
    PayrollApprovalJpa jpa = null;
    if (payrollApproval.getId() != null) {
      jpa = repository.findById(payrollApproval.getId()).orElse(null);
    }
    if (jpa == null) {
      jpa = mapper.toJpa(payrollApproval);
    } else {
      mapper.updateJpa(payrollApproval, jpa);
    }
    PayrollApprovalJpa saved = repository.save(jpa);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<PayrollApproval> findByRunRef(UUID runRef) {
    return repository.findByRunRef(runRef).map(mapper::toDomain);
  }

  @Override
  public Optional<PayrollApproval> findByApprovalId(UUID approvalId) {
    return repository.findById(approvalId).map(mapper::toDomain);
  }
}
