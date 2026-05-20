package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Lanzada cuando se intenta crear un TimeEntry retroactivo (P-TM32) en un AttendanceLedger cuyo
 * periodo ya fue CLOSED y transmitido a nómina.
 *
 * <p>Correcciones en periodos cerrados deben seguir el proceso de Reliquidación.
 */
public final class PeriodLockedException extends DomainException {

  private static final String CODE = "TM-DOMAIN-005";

  public PeriodLockedException(UUID ledgerId) {
    super(
        CODE,
        Map.of("ledgerId", ledgerId),
        "AttendanceLedger ["
            + ledgerId
            + "] belongs to a CLOSED and transmitted period. "
            + "Retroactive modifications are not allowed (P-TM32). Use Reliquidacion process.");
  }
}
