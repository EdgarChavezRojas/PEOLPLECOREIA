package com.solveria.core.legal.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record AddendumSalaryAdjustmentApprovedEvent(
    UUID contractId, UUID addendumId, Instant occurredAt)
    implements DomainEvent {}
