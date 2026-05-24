package com.solveria.core.dossier.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReturnAssetCommand(
    UUID assignmentId, LocalDateTime returnedAt, boolean damaged, String location, UUID tenantId) {}
