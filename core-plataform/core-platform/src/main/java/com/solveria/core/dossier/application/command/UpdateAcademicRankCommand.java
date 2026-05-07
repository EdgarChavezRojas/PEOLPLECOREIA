package com.solveria.core.dossier.application.command;

import java.util.UUID;

public record UpdateAcademicRankCommand(
    UUID relationshipId,
    UUID documentId,
    String courseName,
    String location,
    UUID tenantId,
    String tenantSegment) {}
