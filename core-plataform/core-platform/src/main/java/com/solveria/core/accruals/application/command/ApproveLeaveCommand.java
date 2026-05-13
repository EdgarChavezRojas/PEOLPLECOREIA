package com.solveria.core.accruals.application.command;

import java.util.UUID;

public record ApproveLeaveCommand(UUID balanceId, UUID transactionId, String location) {}

