package com.solveria.core.experience.application.command;

import com.solveria.core.experience.domain.model.vo.ModelType;

/** Command: Registro de un nuevo modelo predictivo IA (Aggregate 11). */
public record RegisterPredictionModelCommand(ModelType modelType, String version) {}
