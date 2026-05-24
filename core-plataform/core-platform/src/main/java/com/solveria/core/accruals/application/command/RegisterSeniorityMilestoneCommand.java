package com.solveria.core.accruals.application.command;

import com.solveria.core.accruals.domain.model.vo.SeniorityBaseType;
import java.util.UUID;

public record RegisterSeniorityMilestoneCommand(
    UUID balanceId, int monthsCompleted, SeniorityBaseType baseSmnType, String location) {}
