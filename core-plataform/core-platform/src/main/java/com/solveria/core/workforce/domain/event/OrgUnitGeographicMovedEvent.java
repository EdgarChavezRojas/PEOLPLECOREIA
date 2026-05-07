package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record OrgUnitGeographicMovedEvent(UUID unitId, String geoCoords, Instant occurredAt)
    implements DomainEvent {}

