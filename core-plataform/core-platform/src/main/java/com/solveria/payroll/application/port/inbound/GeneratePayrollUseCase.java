package com.solveria.payroll.application.port.inbound;

import com.solveria.payroll.application.dto.request.GeneratePayrollRequest;
import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import java.util.UUID;

public interface GeneratePayrollUseCase {
  PayrollRunResponse execute(GeneratePayrollRequest request, UUID tenantId);
}
