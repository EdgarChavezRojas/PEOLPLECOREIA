package com.solveria.TimeAndBearings.domain.event;

import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio publicado cuando un MSS resuelve manualmente un {@code TimeDeviationRecord}
 * (WF-TM02, pasos 3–5).
 *
 * <p>Complementa al {@link ExceptionAutoClosedEvent} que cubre el cierre automático; este evento
 * cubre la resolución humana (APPROVED, REJECTED, OVERRIDDEN_BY_MANAGER, JUSTIFIED_LEAVE,
 * JUSTIFIED_DOCUMENT).
 *
 * <p>Downstream: publicado al Message Broker como {@code TIME_DEVIATION_JUSTIFIED}. BC-01 Core
 * (ESS/MSS Notifications) lo consume para notificar al colaborador el resultado de la justificación
 * de su excepción.
 *
 * @param eventId UUID único del evento (idempotencia).
 * @param ledgerId UUID del AttendanceLedger que contiene la desviación.
 * @param deviationId UUID del TimeDeviationRecord resuelto.
 * @param managerId UUID del MSS/Analista que resolvió la desviación.
 * @param resolutionStatus Estado resultante de la resolución.
 * @param occurredAt Momento en que el evento fue emitido.
 * @param tenantId Partición multi-tenant.
 */
public record TimeDeviationJustifiedEvent(
    UUID eventId,
    UUID ledgerId,
    UUID deviationId,
    UUID managerId,
    ResolutionStatus resolutionStatus,
    Instant occurredAt,
    UUID tenantId)
    implements DomainEvent {

  /** Guard clause: garantiza que el payload del evento nunca esté incompleto. */
  public TimeDeviationJustifiedEvent {
    Objects.requireNonNull(eventId, "eventId es requerido");
    Objects.requireNonNull(ledgerId, "ledgerId es requerido");
    Objects.requireNonNull(deviationId, "deviationId es requerido");
    Objects.requireNonNull(managerId, "managerId es requerido");
    Objects.requireNonNull(resolutionStatus, "resolutionStatus es requerido");
    Objects.requireNonNull(occurredAt, "occurredAt es requerido");
    Objects.requireNonNull(tenantId, "tenantId es requerido");
  }

  /**
   * Factory que construye el evento con un nuevo {@code eventId}.
   *
   * @param ledgerId UUID del AttendanceLedger
   * @param deviationId UUID de la desviación resuelta
   * @param managerId UUID del MSS que resolvió
   * @param resolutionStatus estado resultante
   * @param serverInstant instante del servidor NTP
   * @param tenantId tenant
   * @return nuevo evento de dominio listo para publicar vía Outbox
   */
  public static TimeDeviationJustifiedEvent of(
      UUID ledgerId,
      UUID deviationId,
      UUID managerId,
      ResolutionStatus resolutionStatus,
      Instant serverInstant,
      UUID tenantId) {
    return new TimeDeviationJustifiedEvent(
        UUID.randomUUID(),
        ledgerId,
        deviationId,
        managerId,
        resolutionStatus,
        serverInstant,
        tenantId);
  }

  @Override
  public Instant occurredAt() {
    return occurredAt;
  }
}
