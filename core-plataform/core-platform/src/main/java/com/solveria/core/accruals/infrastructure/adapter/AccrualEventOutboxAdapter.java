package com.solveria.core.accruals.infrastructure.adapter;

import com.solveria.core.accruals.application.port.EventOutboxPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccrualEventOutboxAdapter implements EventOutboxPort {

  private final com.solveria.core.workforce.application.port.EventOutboxPort delegate;

  @Override
  public void publish(String aggregateType, UUID aggregateId, String eventType, String payload) {
    delegate.publish(aggregateType, aggregateId, eventType, payload);
  }
}
