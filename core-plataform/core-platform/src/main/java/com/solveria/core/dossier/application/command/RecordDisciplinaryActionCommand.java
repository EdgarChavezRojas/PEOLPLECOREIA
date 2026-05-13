package com.solveria.core.dossier.application.command;

import com.solveria.core.dossier.domain.model.vo.DisciplinarySeverity;
import java.time.LocalDate;
import java.util.UUID;

public record RecordDisciplinaryActionCommand(
    UUID employeeId,
    DisciplinarySeverity severity,
    String reason,
    byte[] evidenceContent,
    String evidenceFileName,
    LocalDate evidenceExpiryDate,
    String location,
    UUID tenantId) {}

