package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando se intenta ejecutar el cierre de un
 * {@code TimesheetPeriod} y quedan {@code AttendanceLedger} sin cerrar (P-TM33).
 *
 * <p><b>Invariante protegida:</b> El {@code TimesheetPeriod} NO puede transicionar
 * a {@code CLOSED} ni emitir {@code ATTENDANCE_PERIOD_CLOSED} si existen ledgers
 * con status distinto a {@code CLOSED} dentro del rango del periodo.
 *
 * <p>Código de error: {@code TM.PERIOD.PENDING_LEDGERS}
 */
public final class PendingLedgersBlockClosureException extends DomainException {

    private static final String CODE = "TM.PERIOD.PENDING_LEDGERS";

    /**
     * @param periodId   identificador del {@code TimesheetPeriod} que intenta cerrarse
     * @param pendingCount número de ledgers aún pendientes (status != CLOSED)
     */
    public PendingLedgersBlockClosureException(UUID periodId, int pendingCount) {
        super(
                CODE,
                Map.of("periodId", periodId.toString(), "pendingCount", pendingCount),
                "El periodo [%s] tiene [%d] AttendanceLedger(s) sin cerrar. El cierre está bloqueado (P-TM33)."
                        .formatted(periodId, pendingCount));
    }
}
