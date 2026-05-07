package com.solveria.core.dossier.application.command;

import java.util.UUID;

public record CheckOffboardingAssetsCommand(UUID workerId, String location, UUID tenantId) {}
