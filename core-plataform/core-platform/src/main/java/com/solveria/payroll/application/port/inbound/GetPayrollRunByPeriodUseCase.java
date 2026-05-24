package com.solveria.payroll.application.port.inbound;

import com.solveria.payroll.application.dto.response.PayrollRunDetailResponse;
import java.util.UUID;

public interface GetPayrollRunByPeriodUseCase {
  PayrollRunDetailResponse execute(UUID periodId, UUID tenantId);
}
