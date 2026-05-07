package com.solveria.TimeAndBearings.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.TimeAndBearings.domain.model.enums.DeviationType;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio publicado al detectar una anomalía de marcación en tiempo real.
 *
 * <p>Disparado por WF-TM01 (paso 8) cuando se detecta {@code LATE_IN} o {@code GEO_VIOLATION}.
 * La capa de notificaciones de BC-01 Core lo convierte en push notification al MSS.
 * BC-TM ya terminó el flujo de marcación antes de publicar este evento (Non-Blocking Design).
 *
 * <p>Downstream: publicado al Message Broker como {@code PUNCH_ANOMALY_DETECTED}.
 *
 * @param ledgerId      ID del AttendanceLedger afectado.
 * @param relationshipId ID del colaborador.
 * @param deviationType Tipo de anomalía detectada (LATE_IN o GEO_VIOLATION).
 * @param detectedAt    Momento exacto del servidor NTP en que se detectó la anomalía.
 * @param tenantId      Partición multi-tenant.
 */
public record PunchAnomalyDetectedEvent(
        UUID ledgerId,
        UUID relationshipId,
        DeviationType deviationType,
        Instant detectedAt,
        UUID tenantId
) implements DomainEvent {

    @Override
    public Instant occurredAt() {
        return detectedAt;
    }
}
