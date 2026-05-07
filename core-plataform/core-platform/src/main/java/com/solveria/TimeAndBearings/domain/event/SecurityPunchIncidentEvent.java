package com.solveria.TimeAndBearings.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: SecurityPunchIncidentEvent.
 * Emitido cuando {@code PunchAttemptLog.securityIncident = TRUE} (P-TM30 Proxy Clocking Detection).
 *
 * <p>Downstream consumers (BC-01 Core Security &amp; Audit) crean un {@code SecurityIncidentRecord}
 * con nivel CRITICAL. El dispositivo físico continúa operativo (Non-Blocking Design).
 *
 * <p>Publicado en el Message Broker como evento asíncrono {@code SECURITY_PUNCH_INCIDENT}
 * (BC-TM v1.2 – Mapa de Dependencias Downstream, WF-TM04).
 *
 * @param attemptId       UUID del PunchAttemptLog que originó el incidente.
 * @param deviceId        UUID del ClockingDevice donde ocurrió el intento.
 * @param relationshipId  UUID opaco del colaborador involucrado.
 * @param incidentType    Tipo de incidente: "PROXY_CLOCKING", "REVOKED_CREDENTIAL", etc.
 * @param attemptedAt     Hora del servidor NTP del intento fraudulento.
 * @param occurredAt      Momento en que el evento de dominio fue emitido.
 * @param tenantId        Tenant para particionamiento multi-tenant.
 */
public record SecurityPunchIncidentEvent(
        UUID attemptId,
        UUID deviceId,
        UUID relationshipId,
        String incidentType,
        Instant attemptedAt,
        Instant occurredAt,
        UUID tenantId
) implements DomainEvent {

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
