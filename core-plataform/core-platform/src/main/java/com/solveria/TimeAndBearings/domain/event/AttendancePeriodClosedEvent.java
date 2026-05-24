package com.solveria.TimeAndBearings.domain.event;

import com.solveria.TimeAndBearings.domain.model.entity.PayrollHandoffPackage;
import com.solveria.TimeAndBearings.domain.model.enums.ClosureType;
import com.solveria.TimeAndBearings.domain.model.vo.PeriodBoundary;
import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio publicado por {@code TimesheetPeriod} al finalizar el cierre de un periodo
 * (WF-TM03, paso 7).
 *
 * <p>Es el mecanismo exclusivo por el que BC-05 (Financial &amp; Payroll) recibe los datos de
 * asistencia. BC-05 se suscribe a este evento en el Message Broker; NUNCA accede directamente a los
 * {@code AttendanceLedger} de BC-TM.
 *
 * <p><b>Payload:</b> El {@code PayrollHandoffPackage} completo (con todos los {@code
 * EmployeeHandoffRecord}) se incluye en el evento para garantizar que BC-05 tenga todo lo necesario
 * sin necesidad de re-consultar a BC-TM.
 *
 * <p><b>Contrato de versión:</b> v1.0. BC-05 se suscribe a una versión específica. Evoluciones del
 * modelo interno de BC-TM solo requieren actualizar el adaptador de la ACL, no el contrato de este
 * evento.
 *
 * <p><b>Idempotencia:</b> BC-TM garantiza que publicar el mismo {@code periodId} dos veces produce
 * el mismo resultado en BC-05.
 */
public record AttendancePeriodClosedEvent(
    UUID eventId,
    UUID periodId,
    UUID orgUnitId,
    UUID tenantId,
    PeriodBoundary periodBoundary,
    ClosureType closureType,
    PayrollHandoffPackage handoffPackage,
    Instant occurredAt)
    implements DomainEvent {

  /** Guard clause: garantiza que el payload del evento nunca esté incompleto. */
  public AttendancePeriodClosedEvent {
    Objects.requireNonNull(eventId, "eventId es requerido");
    Objects.requireNonNull(periodId, "periodId es requerido");
    Objects.requireNonNull(orgUnitId, "orgUnitId es requerido");
    Objects.requireNonNull(tenantId, "tenantId es requerido");
    Objects.requireNonNull(periodBoundary, "periodBoundary es requerido");
    Objects.requireNonNull(closureType, "closureType es requerido");
    Objects.requireNonNull(handoffPackage, "handoffPackage es requerido");
    Objects.requireNonNull(occurredAt, "occurredAt es requerido");

    if (!handoffPackage.getPeriodId().equals(periodId)) {
      throw new IllegalArgumentException(
          "El periodId del handoffPackage [%s] no coincide con el periodId del evento [%s]"
              .formatted(handoffPackage.getPeriodId(), periodId));
    }
  }

  /**
   * Factory que construye el evento con un nuevo {@code eventId} y el timestamp actual del servidor
   * NTP.
   *
   * @param periodId id del periodo cerrado
   * @param orgUnitId unidad organizacional del periodo
   * @param tenantId tenant del periodo
   * @param periodBoundary límites temporales del periodo
   * @param closureType tipo de cierre (MANUAL/AUTO)
   * @param handoffPackage paquete de datos para BC-05
   * @param serverInstant instante del servidor NTP
   * @return nuevo evento de dominio listo para publicar
   */
  public static AttendancePeriodClosedEvent of(
      UUID periodId,
      UUID orgUnitId,
      UUID tenantId,
      PeriodBoundary periodBoundary,
      ClosureType closureType,
      PayrollHandoffPackage handoffPackage,
      Instant serverInstant) {
    return new AttendancePeriodClosedEvent(
        UUID.randomUUID(),
        periodId,
        orgUnitId,
        tenantId,
        periodBoundary,
        closureType,
        handoffPackage,
        serverInstant);
  }
}
