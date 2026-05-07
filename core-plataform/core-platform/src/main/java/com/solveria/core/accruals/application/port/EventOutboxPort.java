package com.solveria.core.accruals.application.port;

import java.util.UUID;

public interface EventOutboxPort {

  void publish(String aggregateType, UUID aggregateId, String eventType, String payload);
}
