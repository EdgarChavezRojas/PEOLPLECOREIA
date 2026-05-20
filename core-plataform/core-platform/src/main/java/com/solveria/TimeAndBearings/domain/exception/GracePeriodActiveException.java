package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando se intenta emitir el evento {@code ATTENDANCE_PERIOD_CLOSED}
 * antes de que el Periodo de Gracia haya vencido en ausencia de cierre manual explícito (P-TM34).
 *
 * <p><b>Política P-TM34:</b> Durante el Periodo de Gracia (period_end &lt; ahora ≤
 * grace_period_end), los ledgers siguen aceptando modificaciones y NO se puede emitir el evento de
 * nómina. Solo al vencer el grace_period_end el CRON puede ejecutar el cierre masivo automático.
 *
 * <p>Código de error: {@code TM.PERIOD.GRACE_ACTIVE}
 */
public final class GracePeriodActiveException extends DomainException {

  private static final String CODE = "TM.PERIOD.GRACE_ACTIVE";

  /**
   * @param periodId identificador del {@code TimesheetPeriod}
   * @param gracePeriodEnd timestamp en que vence el periodo de gracia
   */
  public GracePeriodActiveException(UUID periodId, LocalDateTime gracePeriodEnd) {
    super(
        CODE,
        Map.of("periodId", periodId.toString(), "gracePeriodEnd", gracePeriodEnd.toString()),
        "El Periodo de Gracia del TimesheetPeriod [%s] sigue activo hasta [%s] (P-TM34). "
            + "El cierre automático no puede ejecutarse aún.".formatted(periodId, gracePeriodEnd));
  }
}
