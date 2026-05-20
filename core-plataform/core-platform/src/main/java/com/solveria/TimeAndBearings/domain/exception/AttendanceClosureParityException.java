package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Lanzada al intentar transicionar un AttendanceLedger a CLOSED cuando existe un número impar de
 * TimeEntry {PUNCH_IN, PUNCH_OUT} vigentes sin ser cubiertos por un TimeDeviationRecord de tipo
 * MISSING_PUNCH con estado OVERRIDDEN_BY_MANAGER.
 *
 * <p>Enforces: Invariante de Paridad para el Cierre del Ledger (Attendance Closure Parity). La
 * nómina opera sobre datos matemáticamente completos; transmitir un ledger abierto a BC-05 causaría
 * pagos incorrectos.
 */
public final class AttendanceClosureParityException extends DomainException {

  private static final String CODE = "TM-DOMAIN-004";

  public AttendanceClosureParityException(UUID ledgerId, int uncoveredOpenPunches) {
    super(
        CODE,
        Map.of("ledgerId", ledgerId, "uncoveredOpenPunches", uncoveredOpenPunches),
        "AttendanceLedger ["
            + ledgerId
            + "] cannot transition to CLOSED: "
            + uncoveredOpenPunches
            + " PUNCH_IN(s) without matching PUNCH_OUT and no "
            + "OVERRIDDEN_BY_MANAGER MISSING_PUNCH deviation covering the gap (Closure Parity invariant).");
  }
}
