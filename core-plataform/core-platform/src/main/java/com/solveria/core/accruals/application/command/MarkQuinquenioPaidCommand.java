package com.solveria.core.accruals.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record MarkQuinquenioPaidCommand(
    UUID relationshipId, LocalDate paymentDate, String location) {}

