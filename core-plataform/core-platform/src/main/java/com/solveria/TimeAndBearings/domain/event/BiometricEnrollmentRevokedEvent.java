package com.solveria.TimeAndBearings.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: BiometricEnrollmentRevokedEvent.
 * Emitido por {@code ClockingDevice} cuando un BiometricEnrollment es revocado.
 *
 * <p>Dispara la sincronización de la lista de templates en el dispositivo físico.
 * El dispositivo recibe señal en su próximo heartbeat para eliminar el template local (WF-TM04 paso 2).
 *
 * @param enrollmentId   UUID del BiometricEnrollment revocado.
 * @param deviceId       UUID del ClockingDevice afectado.
 * @param relationshipId UUID del colaborador cuyo template fue revocado.
 * @param revocationReason Razón de la revocación.
 * @param occurredAt     Momento del evento.
 * @param tenantId       Tenant para particionamiento multi-tenant.
 */
public record BiometricEnrollmentRevokedEvent(
        UUID enrollmentId,
        UUID deviceId,
        UUID relationshipId,
        String revocationReason,
        Instant occurredAt,
        UUID tenantId
) implements DomainEvent {

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
