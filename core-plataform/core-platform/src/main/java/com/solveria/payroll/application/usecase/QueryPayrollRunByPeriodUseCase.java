package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.dto.response.PayrollLineResponse;
import com.solveria.payroll.application.dto.response.PayrollRunDetailResponse;
import com.solveria.payroll.application.port.inbound.GetPayrollRunByPeriodUseCase;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QueryPayrollRunByPeriodUseCase implements GetPayrollRunByPeriodUseCase {

  private final PayrollRunRepositoryPort repository;

  public QueryPayrollRunByPeriodUseCase(PayrollRunRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PayrollRunDetailResponse> execute(UUID periodId, UUID tenantId) {
    List<PayrollRun> runs = repository.findByPeriodAndTenant(periodId, tenantId);
    if (runs.isEmpty()) {
      throw new IllegalArgumentException(
          "No se encontró ninguna planilla para el período y tenant especificados.");
    }

    return runs.stream().map(this::toDetailResponse).toList();
  }

  private PayrollRunDetailResponse toDetailResponse(PayrollRun run) {
    BigDecimal totalGross = BigDecimal.ZERO;
    BigDecimal totalNet = BigDecimal.ZERO;

    List<PayrollLineResponse> lineResponses =
        run.getLines().stream()
            .map(
                line ->
                    new PayrollLineResponse(
                        line.getId(),
                        line.getEmployeeId(),
                        line.getBasicSalary(),
                        line.getSeniorityBonus(),
                        line.getTotalEarned(),
                        line.getRcIvaRetained(),
                        line.getGestoraRetained(),
                        line.getInfocalRetained(),
                        line.getOtherDeductions(),
                        line.getNetPayable()))
            .toList();

    for (PayrollLineResponse line : lineResponses) {
      totalGross = totalGross.add(line.totalEarned());
      totalNet = totalNet.add(line.netPayable());
    }

    return new PayrollRunDetailResponse(
        run.getId(),
        run.getPeriodRef(),
        run.getTenantId(),
        run.getRunType().name(),
        run.getStatus().name(),
        totalGross,
        totalNet,
        lineResponses);
  }
}
