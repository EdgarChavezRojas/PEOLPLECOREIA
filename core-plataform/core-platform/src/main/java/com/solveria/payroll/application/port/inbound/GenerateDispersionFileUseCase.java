package com.solveria.payroll.application.port.inbound;

import java.util.UUID;

public interface GenerateDispersionFileUseCase {
    void execute(UUID runRef, UUID bankEntityRef, String tenantId);
}
