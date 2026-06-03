package com.solveria.core.dossier.application.command;

import java.time.LocalDate;

public record EvaluateDocumentExpirationsCommand(LocalDate today) {}
