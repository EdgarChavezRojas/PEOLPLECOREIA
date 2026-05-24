package com.solveria.core.accruals.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ProvisionQuinquenioCommand(
    UUID relationshipId, BigDecimal monthlyAmount, String location) {}
