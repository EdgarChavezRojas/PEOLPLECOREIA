package com.solveria.payroll.application.port.inbound;

import com.solveria.payroll.application.dto.request.ApprovePayrollRequest;
import java.util.UUID;

public interface ApprovePayrollUseCase {
  void execute(UUID runId, ApprovePayrollRequest request, UUID tenantId);
}
