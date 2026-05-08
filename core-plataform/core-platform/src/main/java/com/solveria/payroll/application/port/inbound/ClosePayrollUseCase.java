package com.solveria.payroll.application.port.inbound;

import java.util.UUID;

public interface ClosePayrollUseCase {
    void execute(UUID runRef, String tenantId);
}
