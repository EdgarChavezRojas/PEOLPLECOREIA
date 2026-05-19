package com.solveria.core.shared.events;


import java.time.Instant;
import java.util.UUID;



/**
 * Evento (Async): Memorando acusado de recibo por el empleado.
 * Unificado para soportar la emisión desde BC6 (Notification) y BC3 (DocumentRecord).
 */
public record MemorandumAcknowledgedEvent(
        UUID notificationId,
        UUID documentId,
        UUID relationshipId,
        UUID personId,
        UUID tenantId,
        byte[] digitalSignature, // <-- NUEVO: La firma criptográfica real
        String locationCode,
        Instant acknowledgedAt,
        Instant occurredAt
) implements DomainEvent {

    public MemorandumAcknowledgedEvent {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    // 1. Constructor para el BC6 (Notification Aggregate)
    public MemorandumAcknowledgedEvent(UUID notificationId, UUID personId, Instant acknowledgedAt, UUID tenantId) {
        this(notificationId, null, null, personId, tenantId,null,null, acknowledgedAt, Instant.now());
    }

    // 2. Método estático para el BC3 (DocumentRecord Aggregate)
    public static MemorandumAcknowledgedEvent now(UUID relationshipId) {
        return new MemorandumAcknowledgedEvent(null, null, relationshipId, null, null,null,null, Instant.now(), Instant.now());
    }
}