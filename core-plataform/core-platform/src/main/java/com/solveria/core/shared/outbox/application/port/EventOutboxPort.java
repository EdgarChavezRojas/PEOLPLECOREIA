package com.solveria.core.shared.outbox.application.port;

import com.solveria.core.shared.events.DomainEvent;
import java.util.List;

public interface EventOutboxPort {

  void publish(List<DomainEvent> events);
}
