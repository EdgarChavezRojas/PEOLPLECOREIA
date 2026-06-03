package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import com.solveria.payroll.domain.model.vo.PayrollRunType;
import com.solveria.payroll.domain.model.vo.PayrollStatus;
import com.solveria.payroll.infrastructure.jpa.PayrollRunJpa;
import com.solveria.payroll.infrastructure.mapper.PayrollRunMapper;
import com.solveria.payroll.infrastructure.repository.PayrollRunSpringRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PayrollRunRepositoryAdapter implements PayrollRunRepositoryPort {

  private final PayrollRunSpringRepository repository;
  private final PayrollRunMapper mapper;

  public PayrollRunRepositoryAdapter(
      PayrollRunSpringRepository repository, PayrollRunMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public PayrollRun save(PayrollRun payrollRun) {
    PayrollRunJpa jpa = mapper.toJpa(payrollRun);
    if (jpa.getLines() != null) {
      jpa.getLines().forEach(line -> line.setPayrollRun(jpa));
    }
    PayrollRunJpa saved = repository.save(jpa);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<PayrollRun> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<PayrollRun> findByPeriodAndTenant(UUID periodId, UUID tenantId) {
    return repository.findByPeriodAndTenant(periodId, tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Optional<PayrollRun> findByIdWithLines(UUID runId) {
    return repository.findByIdWithLines(runId).map(mapper::toDomain);
  }

  @Override
  public List<PayrollRun> findAllByTenant(UUID tenantId) {
    List<PayrollRunJpa> rows = repository.findAllByTenantId(tenantId);
    return rows.stream().map(this::toHeaderDomain).toList();
  }

  /**
   * Maps only header fields from the JPA entity to avoid triggering lazy initialization on the
   * lines collection.
   */
  private PayrollRun toHeaderDomain(PayrollRunJpa jpa) {
    return new PayrollRun(
        jpa.getPayrollRunId(),
        jpa.getPeriodRef(),
        jpa.getGroupRef(),
        PayrollRunType.valueOf(jpa.getRunType()),
        PayrollStatus.valueOf(jpa.getStatus()),
        jpa.getTenantId(),
        Collections.emptyList());
  }
}
