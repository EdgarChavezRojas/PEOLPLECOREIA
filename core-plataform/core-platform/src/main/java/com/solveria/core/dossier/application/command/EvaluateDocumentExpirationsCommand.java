package com.solveria.core.dossier.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record EvaluateDocumentExpirationsCommand(
    LocalDate today,
    UUID tenantId) {}

