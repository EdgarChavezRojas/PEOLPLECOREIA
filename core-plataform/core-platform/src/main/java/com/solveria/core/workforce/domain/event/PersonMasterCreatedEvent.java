package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

// revisar porque no esta implementado en ningun caso de uso
public record PersonMasterCreatedEvent(UUID personId, UUID tenantId, Instant occurredAt)
    implements DomainEvent {}
