package com.solveria.core.accruals.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record RequestQuinquenioPaymentCommand(
    UUID relationshipId, LocalDate requestDate, String location) {}

