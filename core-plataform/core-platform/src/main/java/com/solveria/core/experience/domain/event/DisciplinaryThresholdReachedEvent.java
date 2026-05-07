package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Umbral disciplinario alcanzado detectado por IA. Ejemplo: 3 memorandos en 6 meses
 * -> posible Despido Justificado. Genera alerta CRITICAL para el área de RRHH.
 */
public record DisciplinaryThresholdReachedEvent(
    UUID modelId,
    UUID personId,
    int memorandumCount,
    int periodMonths,
    String recommendation,
    String tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public DisciplinaryThresholdReachedEvent(
      UUID modelId,
      UUID personId,
      int memorandumCount,
      int periodMonths,
      String recommendation,
      String tenantId) {
    this(modelId, personId, memorandumCount, periodMonths, recommendation, tenantId, Instant.now());
  }
}
