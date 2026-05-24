package com.solveria.payroll.application.port.inbound;

import com.solveria.payroll.application.dto.response.PaySlipResponse;
import java.util.UUID;

public interface GetEmployeePaySlipUseCase {
  PaySlipResponse execute(UUID runId, UUID employeeId, UUID tenantId);
}
