package com.solveria.TimeAndBearings.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio publicado durante la consolidación diaria (WF-TM03, CRON nocturno).
 *
 * <p>Downstream: publicado al Message Broker como {@code ATTENDANCE_SUMMARY_FOR_ROSTER}. BC-SCH
 * (Scheduling) lo consume para alimentar el motor de recomendación de turnos con métricas de
 * asistencia real del colaborador.
 *
 * <p><b>Payload definido en BC-TM v1.2 — Mapa de Dependencias Downstream:</b> {@code work_date},
 * {@code relationship_id}, {@code total_hours}, {@code attendance_rate_last_30d}.
 *
 * @param eventId UUID único del evento (idempotencia).
 * @param employeeId UUID opaco del colaborador ({@code relationship_id} en BC-01).
 * @param workDate Día calendario consolidado.
 * @param totalHours Horas totales netas pagables del día ({@code net_payable_hours}).
 * @param rateLast30d Tasa de asistencia de los últimos 30 días (0.00 – 1.00).
 * @param occurredAt Momento en que el evento fue emitido.
 * @param tenantId Partición multi-tenant.
 */
public record AttendanceSummaryForRosterEvent(
    UUID eventId,
    UUID employeeId,
    LocalDate workDate,
    BigDecimal totalHours,
    BigDecimal rateLast30d,
    Instant occurredAt,
    UUID tenantId)
    implements DomainEvent {

  /** Guard clause: garantiza que el payload del evento nunca esté incompleto. */
  public AttendanceSummaryForRosterEvent {
    Objects.requireNonNull(eventId, "eventId es requerido");
    Objects.requireNonNull(employeeId, "employeeId es requerido");
    Objects.requireNonNull(workDate, "workDate es requerido");
    Objects.requireNonNull(totalHours, "totalHours es requerido");
    Objects.requireNonNull(rateLast30d, "rateLast30d es requerido");
    Objects.requireNonNull(occurredAt, "occurredAt es requerido");
    Objects.requireNonNull(tenantId, "tenantId es requerido");
  }

  /**
   * Factory que construye el evento con un nuevo {@code eventId} y el timestamp proporcionado.
   *
   * @param employeeId relationship_id del colaborador
   * @param workDate día consolidado
   * @param totalHours horas netas pagables del día
   * @param rateLast30d tasa de asistencia últimos 30 días
   * @param serverInstant instante del servidor NTP
   * @param tenantId tenant
   * @return nuevo evento de dominio listo para publicar vía Outbox
   */
  public static AttendanceSummaryForRosterEvent of(
      UUID employeeId,
      LocalDate workDate,
      BigDecimal totalHours,
      BigDecimal rateLast30d,
      Instant serverInstant,
      UUID tenantId) {
    return new AttendanceSummaryForRosterEvent(
        UUID.randomUUID(), employeeId, workDate, totalHours, rateLast30d, serverInstant, tenantId);
  }

  @Override
  public Instant occurredAt() {
    return occurredAt;
  }
}
