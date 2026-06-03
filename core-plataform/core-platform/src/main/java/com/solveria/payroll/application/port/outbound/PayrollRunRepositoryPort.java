package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.ar.PayrollRun;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollRunRepositoryPort {
  PayrollRun save(PayrollRun payrollRun);

  Optional<PayrollRun> findById(UUID id);

  List<PayrollRun> findByPeriodAndTenant(UUID periodId, UUID tenantId);

  Optional<PayrollRun> findByIdWithLines(UUID runId);

  List<PayrollRun> findAllByTenant(UUID tenantId);
}
