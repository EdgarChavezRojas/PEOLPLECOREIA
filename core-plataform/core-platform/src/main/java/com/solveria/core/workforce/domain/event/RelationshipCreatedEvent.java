package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record RelationshipCreatedEvent(
    UUID relationshipId, UUID personId, UUID tenantId, Instant occurredAt) implements DomainEvent {}
