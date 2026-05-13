package com.solveria.core.accruals.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record EvaluateQuinquenioPenaltyCommand(
    UUID relationshipId,
    LocalDate requestDate,
    LocalDate paymentDate,
    LocalDate today,
    String location) {}

