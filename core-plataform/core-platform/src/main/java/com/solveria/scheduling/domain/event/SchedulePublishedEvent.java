package com.solveria.scheduling.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record SchedulePublishedEvent(
    UUID planId,
    Instant occurredAt
) implements DomainEvent {
}
