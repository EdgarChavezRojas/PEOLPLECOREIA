package com.solveria.core.dossier.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddPerformanceSnapshotCommand(
    UUID relationshipId, String evalPeriod, BigDecimal score, String location, UUID tenantId) {}
