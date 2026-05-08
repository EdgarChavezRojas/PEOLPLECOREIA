package com.solveria.payroll.domain.model.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollPeriodClosedEvent(
        UUID runId,
        String integrityHash,
        String tenantId,
        LocalDateTime closedAt
) {
}
