package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record AcademicProfileRankUpdatedEvent(
    UUID relationshipId, String newRank, Instant occurredAt) implements DomainEvent {}
