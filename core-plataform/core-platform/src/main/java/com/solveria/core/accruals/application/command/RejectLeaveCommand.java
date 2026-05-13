package com.solveria.core.accruals.application.command;

import java.util.UUID;

public record RejectLeaveCommand(UUID balanceId, UUID transactionId, String location) {}

