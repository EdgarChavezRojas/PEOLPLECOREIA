package com.solveria.scheduling.domain.model.vo;

import com.solveria.scheduling.domain.model.enums.DeviationType;

/**
 * Value Object para representar una desviación de tiempo (ej. Llegada tardía, horas extra).
 * Se persiste como JSON.
 *
 * @param deviationType Tipo de desviación
 * @param minutes Cantidad de minutos de la desviación
 * @param approvalStatus Estado de aprobación (ej. PENDING, APPROVED, REJECTED)
 */
public record TimeDeviation(
    DeviationType deviationType,
    Integer minutes,
    String approvalStatus
) {
}
