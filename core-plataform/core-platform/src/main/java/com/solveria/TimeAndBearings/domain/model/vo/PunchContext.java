package com.solveria.TimeAndBearings.domain.model.vo;

import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import java.util.UUID;

/**
 * Snapshot inmutable del contexto técnico de la marcación. Value Object – Aggregate 14:
 * AttendanceLedger.
 *
 * <p>Capturado en el paso 2 del WF-TM01 (Captura de Contexto Inmutable). No contiene hora de
 * marcación; esa responsabilidad recae en TimeEntry.punchTime (P-TM26).
 *
 * @param deviceId FK a ClockingDevice (Aggregate 15). NULL si source=MANUAL o WEB.
 * @param sourceChannel Canal de origen que determina el nivel de autenticación (P-TM29).
 * @param ipAddress IPv4 o IPv6. NOT NULL para canales web/móvil.
 * @param userAgent User-agent del cliente. Para auditoría de canal.
 */
public record PunchContext(
    UUID deviceId, PunchSource sourceChannel, String ipAddress, String userAgent) {

  /** Invariante: ipAddress no puede ser nulo ni vacío para canales MOBILE y WEB. */
  public PunchContext {
    if ((sourceChannel == PunchSource.MOBILE || sourceChannel == PunchSource.WEB)
        && (ipAddress == null || ipAddress.isBlank())) {
      throw new IllegalArgumentException(
          "PunchContext: ipAddress is mandatory for MOBILE and WEB channels.");
    }
    if ((sourceChannel == PunchSource.KIOSK || sourceChannel == PunchSource.BIOMETRIC_READER)
        && deviceId == null) {
      throw new IllegalArgumentException(
          "PunchContext: deviceId is mandatory for KIOSK and BIOMETRIC_READER channels.");
    }
  }
}
