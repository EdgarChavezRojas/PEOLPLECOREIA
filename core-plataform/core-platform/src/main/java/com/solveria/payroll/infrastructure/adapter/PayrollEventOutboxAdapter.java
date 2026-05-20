package com.solveria.payroll.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.shared.outbox.domain.OutboxState;
import com.solveria.core.shared.outbox.infrastructure.jpa.SharedOutboxMessageJpaEntity;
import com.solveria.core.shared.outbox.infrastructure.repository.SharedOutboxRepository;
import com.solveria.payroll.application.port.outbound.EventOutboxPort;
import com.solveria.payroll.domain.model.event.PayrollPeriodClosedEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollEventOutboxAdapter implements EventOutboxPort {

  private final SharedOutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public PayrollEventOutboxAdapter(
      SharedOutboxRepository outboxRepository, ObjectMapper objectMapper) {
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void publish(PayrollPeriodClosedEvent event) {
    if (event == null) {
      return;
    }

    SharedOutboxMessageJpaEntity outboxMessage = toOutboxMessage(event);
    outboxRepository.save(outboxMessage);
  }

  private SharedOutboxMessageJpaEntity toOutboxMessage(PayrollPeriodClosedEvent event) {
    String payload = toJson(event);

    return SharedOutboxMessageJpaEntity.builder()
        .eventId(UUID.randomUUID())
        .aggregateType(resolveAggregateType(event))
        .aggregateId(event.runId())
        .type(event.getClass().getName())
        .payload(payload)
        .state(OutboxState.PENDING)
        .createdAt(LocalDateTime.now())
        .build();
  }

  private String resolveAggregateType(PayrollPeriodClosedEvent event) {
    String simpleName = event.getClass().getSimpleName();
    return simpleName.substring(0, simpleName.length() - "Event".length());
  }

  private String toJson(PayrollPeriodClosedEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("No se pudo serializar el evento para outbox", ex);
    }
  }
}
