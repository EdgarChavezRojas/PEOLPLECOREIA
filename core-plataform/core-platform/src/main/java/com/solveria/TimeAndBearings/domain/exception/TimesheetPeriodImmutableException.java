package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.TimeAndBearings.domain.model.enums.PeriodStatus;
import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando se intenta realizar una operación sobre un {@code
 * TimesheetPeriod} cuyo estado {@code CLOSED} o {@code TRANSMITTED} lo convierte en completamente
 * inmutable (P-TM33).
 *
 * <p>Complementa a la {@code ClosedRecordMutationException} del {@code AttendanceLedger}. Esta
 * excepción es específica del nivel de Aggregate Root {@code TimesheetPeriod}.
 *
 * <p>Código de error: {@code TM.PERIOD.IMMUTABLE}
 */
public final class TimesheetPeriodImmutableException extends DomainException {

  private static final String CODE = "TM.PERIOD.IMMUTABLE";

  /**
   * @param periodId identificador del periodo inmutable
   * @param currentStatus status actual que impide la operación
   */
  public TimesheetPeriodImmutableException(UUID periodId, PeriodStatus currentStatus) {
    super(
        CODE,
        Map.of("periodId", periodId.toString(), "status", currentStatus.name()),
        "El TimesheetPeriod [%s] está en estado [%s] y es completamente inmutable (P-TM33)."
            .formatted(periodId, currentStatus));
  }
}
