package com.solveria.payroll.application.port.inbound;

import com.solveria.payroll.application.dto.request.ReviewPayrollRequest;
import java.util.UUID;

public interface ReviewPayrollUseCase {
  void execute(UUID runId, ReviewPayrollRequest request, UUID tenantId);
}

