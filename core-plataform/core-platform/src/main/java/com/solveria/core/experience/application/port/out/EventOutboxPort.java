package com.solveria.core.experience.application.port.out;

import java.util.UUID;

/**
 * Secondary Port (Outbound): Event Outbox para BC Experience. Patrón Transactional Outbox: persiste
 * eventos de dominio en la BD dentro de la misma transacción para garantizar consistencia.
 */
public interface EventOutboxPort {

  void publish(String aggregateType, UUID aggregateId, String eventType, String payload);
}
