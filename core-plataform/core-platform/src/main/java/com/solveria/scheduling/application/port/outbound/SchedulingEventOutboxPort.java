package com.solveria.scheduling.application.port.outbound;

import com.solveria.core.shared.events.DomainEvent;

public interface SchedulingEventOutboxPort {
    void publish(DomainEvent event);
}
