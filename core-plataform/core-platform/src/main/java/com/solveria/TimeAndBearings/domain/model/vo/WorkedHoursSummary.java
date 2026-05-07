package com.solveria.TimeAndBearings.domain.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado calculado y consolidado de horas trabajadas en la jornada.
 * Generado por el CRON nocturno del WF-TM03.
 * Value Object – Aggregate 14: AttendanceLedger. Inmutable una vez que el Ledger cierra (P-TM33).
 *
 * @param regularHours         Horas ordinarias dentro del turno contratado.
 * @param overtimeHours        Horas extras aprobadas por el MSS.
 * @param nightHours           Horas entre 22:00 y 06:00 (recargo 25% LGT Bolivia).
 * @param holidayHours         Horas en día feriado oficial (recargo 100% LGT Bolivia).
 * @param deductedBreakMinutes Minutos de descanso deducidos automáticamente.
 * @param netPayableHours      CALCULADO: regular + overtime + night + holiday – (deducted/60).
 * @param calculatedAt         Momento del cálculo por el CRON. Inmutable post-CLOSED.
 */
public record WorkedHoursSummary(
        BigDecimal regularHours,
        BigDecimal overtimeHours,
        BigDecimal nightHours,
        BigDecimal holidayHours,
        int deductedBreakMinutes,
        BigDecimal netPayableHours,
        LocalDateTime calculatedAt
) {

    public WorkedHoursSummary {
        if (regularHours == null || regularHours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("WorkedHoursSummary: regularHours cannot be null or negative.");
        }
        if (netPayableHours == null || netPayableHours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("WorkedHoursSummary: netPayableHours cannot be null or negative.");
        }
        if (calculatedAt == null) {
            throw new IllegalArgumentException("WorkedHoursSummary: calculatedAt is mandatory.");
        }
    }
}
