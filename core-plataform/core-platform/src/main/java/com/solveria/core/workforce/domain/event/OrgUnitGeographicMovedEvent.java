package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

// revisar porque no esta siendo implementado en ningun caso de uso o agregado
public record OrgUnitGeographicMovedEvent(
    UUID unitId, String geoCoords, UUID tenantId, Instant occurredAt) implements DomainEvent {

  public OrgUnitGeographicMovedEvent(UUID unitId, String geoCoords, UUID tenantId) {
    this(unitId, geoCoords, tenantId, Instant.now());
  }
}
