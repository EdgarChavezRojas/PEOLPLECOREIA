package com.solveria.core.legal.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record EvidenceGeneratedEvent(UUID contractId, String tenantId, String hash, Instant occurredAt)
    implements DomainEvent {}
