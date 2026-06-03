package com.solveria.payroll.application.port.inbound;

import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import java.util.List;
import java.util.UUID;

public interface ListAllPayrollRunsUseCase {
  List<PayrollRunResponse> execute(UUID tenantId);
}
