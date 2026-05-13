package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Emitido cuando el offboarding se bloquea por activos sin devolver.
 * Trigger: RelationshipTerminationInitiated.
 * El proceso de liquidación (Finiquito) no puede avanzar a "Aprobado"
 * si existen activos marcados como "En Custodia".
 */
public record OffboardingBlockedByAssetsEvent(
    UUID personId,
    List<UUID> unreturnedAssetIds,
    Instant occurredAt
) implements DomainEvent {

  public OffboardingBlockedByAssetsEvent {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido");
    }
    if (unreturnedAssetIds == null || unreturnedAssetIds.isEmpty()) {
      throw new IllegalArgumentException("unreturnedAssetIds es requerido y no puede estar vacío");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
    unreturnedAssetIds = List.copyOf(unreturnedAssetIds);
  }

  public static OffboardingBlockedByAssetsEvent now(UUID personId, List<UUID> unreturnedAssetIds) {
    return new OffboardingBlockedByAssetsEvent(personId, unreturnedAssetIds, Instant.now());
  }
}
//revisar porque evento se genera dentro de usecase