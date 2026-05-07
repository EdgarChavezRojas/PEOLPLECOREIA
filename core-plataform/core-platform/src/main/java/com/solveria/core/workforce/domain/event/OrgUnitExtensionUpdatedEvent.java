package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.workforce.domain.model.vo.CostCenter;
import java.time.Instant;
import java.util.UUID;

public record OrgUnitExtensionUpdatedEvent(UUID unitId, CostCenter costCenter, Instant occurredAt)
    implements DomainEvent {}

