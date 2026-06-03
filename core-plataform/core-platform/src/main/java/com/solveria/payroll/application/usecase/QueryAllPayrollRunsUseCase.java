package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import com.solveria.payroll.application.port.inbound.ListAllPayrollRunsUseCase;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QueryAllPayrollRunsUseCase implements ListAllPayrollRunsUseCase {

  private final PayrollRunRepositoryPort repository;

  public QueryAllPayrollRunsUseCase(PayrollRunRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PayrollRunResponse> execute(UUID tenantId) {
    List<PayrollRun> runs = repository.findAllByTenant(tenantId);
    return runs.stream().map(this::toResponse).toList();
  }

  private PayrollRunResponse toResponse(PayrollRun run) {
    return new PayrollRunResponse(
        run.getId(),
        run.getPeriodRef(),
        run.getTenantId(),
        run.getRunType().name(),
        run.getStatus().name(),
        BigDecimal.ZERO, // totales no se calculan en el listado general
        BigDecimal.ZERO,
        null,
        null);
  }
}
