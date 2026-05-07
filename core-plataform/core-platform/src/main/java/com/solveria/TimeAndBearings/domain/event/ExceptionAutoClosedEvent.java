package com.solveria.TimeAndBearings.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio publicado cuando una excepción vence sin resolución (P-TM31, P-TM34).
 *
 * <p>Downstream: publicado al Message Broker como {@code EXCEPTION_AUTO_CLOSED}.
 * BC-01 Core (ESS/MSS Notifications) lo consume para notificar a los actores relevantes.
 *
 * @param deviationId     ID del TimeDeviationRecord auto-cerrado.
 * @param relationshipId  ID del colaborador afectado.
 * @param finalStatus     Estado resultante (AUTO_CLOSED_AS_UNJUSTIFIED).
 * @param closureReason   Razón del cierre automático (ej. "WINDOW_EXPIRED").
 * @param financialImpact Descripción del impacto en nómina (ej. "FULL_DAY_DEDUCTION").
 * @param occurredAt      Momento del auto-cierre.
 * @param tenantId        Partición multi-tenant.
 */
public record ExceptionAutoClosedEvent(
        UUID deviationId,
        UUID relationshipId,
        ResolutionStatus finalStatus,
        String closureReason,
        String financialImpact,
        Instant occurredAt,
        UUID tenantId
) implements DomainEvent {

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
