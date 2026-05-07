package com.solveria.core.financial.application.port;

import java.util.UUID;

/**
 * Secondary Port (Outbound): Event Outbox para el BC Financial. Patrón Transactional Outbox:
 * persiste eventos de dominio en la BD dentro de la misma transacción para garantizar consistencia.
 */
public interface EventOutboxPort {

  void publish(String aggregateType, UUID aggregateId, String eventType, String payload);
}
