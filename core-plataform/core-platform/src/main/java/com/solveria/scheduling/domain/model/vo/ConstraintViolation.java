package com.solveria.scheduling.domain.model.vo;

import com.solveria.scheduling.domain.model.enums.Severity;

/**
 * Value Object para representar la violación de una restricción (Constraint Engine). Se persiste
 * como JSON.
 *
 * @param ruleCode Código de la regla (ej. "ANTI_CLOPENING")
 * @param severity Severidad (HARD/SOFT)
 * @param description Descripción legible
 */
public record ConstraintViolation(String ruleCode, Severity severity, String description) {}
