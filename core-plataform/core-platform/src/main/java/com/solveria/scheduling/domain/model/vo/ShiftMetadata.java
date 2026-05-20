package com.solveria.scheduling.domain.model.vo;

import java.util.Map;

/**
 * Value Object para metadatos adicionales del turno. Se persiste como JSON.
 *
 * @param notes Notas libres
 * @param properties Propiedades dinámicas adicionales
 */
public record ShiftMetadata(String notes, Map<String, String> properties) {}
