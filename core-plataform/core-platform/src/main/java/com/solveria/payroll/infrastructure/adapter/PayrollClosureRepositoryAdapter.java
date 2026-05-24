package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PayrollClosureRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollClosure;
import com.solveria.payroll.infrastructure.jpa.PayrollClosureJpa;
import com.solveria.payroll.infrastructure.mapper.PayrollClosureMapper;
import com.solveria.payroll.infrastructure.repository.PayrollClosureSpringRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PayrollClosureRepositoryAdapter implements PayrollClosureRepositoryPort {

  private final PayrollClosureSpringRepository repository;
  private final PayrollClosureMapper mapper;

  public PayrollClosureRepositoryAdapter(
      PayrollClosureSpringRepository repository, PayrollClosureMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public PayrollClosure save(PayrollClosure payrollClosure) {
    PayrollClosureJpa jpa = mapper.toJpa(payrollClosure);
    PayrollClosureJpa saved = repository.save(jpa);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<PayrollClosure> findByRunRef(UUID runRef) {
    return repository.findByRunRef(runRef).map(mapper::toDomain);
  }
}
