package com.solveria.scheduling.domain.model.vo;

/**
 * Value Object para la validación de geocerca en la marcación. Se persiste como JSON.
 *
 * @param latitude Latitud de la marcación
 * @param longitude Longitud de la marcación
 * @param isWithinFence Si la marcación fue dentro del radio permitido
 */
public record GeoValidation(Double latitude, Double longitude, Boolean isWithinFence) {}
