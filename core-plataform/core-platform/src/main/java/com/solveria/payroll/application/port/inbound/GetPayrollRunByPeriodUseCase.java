package com.solveria.payroll.application.port.inbound;

import com.solveria.payroll.application.dto.response.PayrollRunDetailResponse;
import java.util.List;
import java.util.UUID;

public interface GetPayrollRunByPeriodUseCase {
  List<PayrollRunDetailResponse> execute(UUID periodId, UUID tenantId);
}
