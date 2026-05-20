package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.dto.request.GeneratePayrollRequest;
import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import com.solveria.payroll.application.port.inbound.GeneratePayrollUseCase;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import com.solveria.payroll.domain.model.vo.PayrollRunType;
import com.solveria.payroll.domain.model.vo.PayrollStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessPayrollGenerationUseCase implements GeneratePayrollUseCase {

  private final PayrollRunRepositoryPort payrollRunRepositoryPort;

  public ProcessPayrollGenerationUseCase(PayrollRunRepositoryPort payrollRunRepositoryPort) {
    this.payrollRunRepositoryPort = payrollRunRepositoryPort;
  }

  @Override
  @Transactional
  public PayrollRunResponse execute(GeneratePayrollRequest request, UUID tenantId) {
    PayrollRun run =
        new PayrollRun(
            UUID.randomUUID(),
            request.periodId(),
            null,
            PayrollRunType.valueOf(request.runType()),
            PayrollStatus.BORRADOR,
            tenantId,
            Collections.emptyList());

    run.generateDraft();
    payrollRunRepositoryPort.save(run);

    return new PayrollRunResponse(
        run.getId(),
        run.getPeriodRef(),
        run.getTenantId(),
        run.getRunType().name(),
        run.getStatus().name(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        LocalDateTime.now(),
        LocalDateTime.now());
  }
}
