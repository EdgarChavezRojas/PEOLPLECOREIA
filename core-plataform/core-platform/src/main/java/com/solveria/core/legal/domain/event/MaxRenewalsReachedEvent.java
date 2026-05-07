package com.solveria.core.legal.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MaxRenewalsReachedEvent(UUID contractId, int renewalCount, Instant occurredAt)
    implements DomainEvent {}
