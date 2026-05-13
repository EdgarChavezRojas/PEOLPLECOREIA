package com.solveria.core.shared.outbox.domain;

import com.solveria.core.shared.events.DomainEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class DomainRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
