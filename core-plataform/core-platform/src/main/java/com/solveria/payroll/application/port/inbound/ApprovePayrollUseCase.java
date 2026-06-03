package com.solveria.payroll.application.port.inbound;

import java.util.UUID;

public interface ApprovePayrollUseCase {
  void execute(UUID runId, UUID tenantId);
}
