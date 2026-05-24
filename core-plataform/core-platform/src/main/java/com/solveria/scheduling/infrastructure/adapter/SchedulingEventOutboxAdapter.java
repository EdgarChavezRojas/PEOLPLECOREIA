package com.solveria.scheduling.infrastructure.adapter;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.scheduling.application.port.outbound.SchedulingEventOutboxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulingEventOutboxAdapter implements SchedulingEventOutboxPort {

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void publish(DomainEvent event) {
    // En la implementación real de El Cartero, aquí se inserta en la tabla EventOutbox
    // Por el momento se delega al event publisher de Spring que los outbox listeners interceptarán
    eventPublisher.publishEvent(event);
  }
}
