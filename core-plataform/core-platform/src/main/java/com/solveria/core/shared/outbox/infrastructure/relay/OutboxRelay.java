package com.solveria.core.shared.outbox.infrastructure.relay;

import com.solveria.core.shared.outbox.domain.OutboxState;
import com.solveria.core.shared.outbox.infrastructure.jpa.SharedOutboxMessageJpaEntity;
import com.solveria.core.shared.outbox.infrastructure.repository.SharedOutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Relay — procesa los eventos de dominio persistidos en la tabla {@code
 * shared_event_outbox}.
 *
 * <h3>Diseño Actual</h3>
 *
 * Los eventos de dominio ya son consumidos sincrónicamente por los {@code @EventListener} de Spring
 * durante la transacción original (al hacer {@code save()} del agregado). El Outbox cumple el rol
 * de <b>registro de auditoría confiable</b> y garantía de entrega at-least-once.
 *
 * <p>Este relay simplemente marca los eventos PENDING como PROCESSED una vez que confirma que el
 * payload es válido (deserializable). NO re-publica los eventos vía {@code
 * ApplicationEventPublisher} para evitar que los listeners se ejecuten dos veces, lo cual causaría
 * un loop infinito de eventos.
 *
 * <h3>Extensión Futura</h3>
 *
 * Cuando se integre un message broker externo (Kafka, RabbitMQ), este relay será el punto de
 * publicación hacia el broker. En ese caso, se reemplazará la lógica de "marcar como procesado" por
 * el envío real al broker.
 */
@Component
public class OutboxRelay {

  private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
  private static final int MAX_RETRY_ATTEMPTS = 5;

  private final SharedOutboxRepository repository;

  public OutboxRelay(SharedOutboxRepository repository) {
    this.repository = repository;
  }

  @Scheduled(fixedDelayString = "${shared.outbox.relay.delay-ms}")
  @Transactional
  public void processPendingEvents() {
    List<SharedOutboxMessageJpaEntity> pending =
        repository.findTop50ByStateOrderByCreatedAtAsc(OutboxState.PENDING);

    if (pending.isEmpty()) {
      return;
    }

    for (SharedOutboxMessageJpaEntity message : pending) {
      LocalDateTime processedAt = LocalDateTime.now();
      try {
        // Validación: verificamos que el payload y el tipo sean consistentes.
        // Los eventos ya fueron procesados por los @EventListener de Spring
        // durante la transacción original del save() del agregado.
        validateMessage(message);
        message.markProcessed(processedAt);

        log.debug(
            "Outbox event marked as processed: eventId={} type={}",
            message.getEventId(),
            message.getType());

      } catch (Exception ex) {
        if (message.getRetryCount() + 1 >= MAX_RETRY_ATTEMPTS) {
          // Agotados los reintentos → marcar como FAILED definitivo
          message.markFailed(
              processedAt,
              "MAX_RETRIES_EXHAUSTED (%d/%d): %s"
                  .formatted(message.getRetryCount() + 1, MAX_RETRY_ATTEMPTS, ex.getMessage()));
          log.error(
              "Outbox event permanently FAILED after {} attempts: eventId={} type={}: {}",
              MAX_RETRY_ATTEMPTS,
              message.getEventId(),
              message.getType(),
              ex.getMessage());
        } else {
          // Aún quedan reintentos → incrementar contador, se mantiene PENDING
          message.incrementRetry(processedAt, ex.getMessage());
          log.warn(
              "Outbox relay attempt {}/{} failed for eventId={} type={}: {}",
              message.getRetryCount(),
              MAX_RETRY_ATTEMPTS,
              message.getEventId(),
              message.getType(),
              ex.getMessage());
        }
      }
      repository.save(message);
    }
  }

  /**
   * Valida que el mensaje del outbox sea consistente: tipo de evento válido y payload no vacío. No
   * se deserializa el payload completo para evitar errores de Jackson con Java Records.
   */
  private void validateMessage(SharedOutboxMessageJpaEntity message) {
    if (message.getType() == null || message.getType().isBlank()) {
      throw new IllegalStateException(
          "Outbox message has null/blank type for eventId=" + message.getEventId());
    }
    if (message.getPayload() == null || message.getPayload().isBlank()) {
      throw new IllegalStateException(
          "Outbox message has null/blank payload for eventId=" + message.getEventId());
    }
    // Verificar que la clase existe en el classpath
    try {
      Class.forName(message.getType());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Unknown event type: " + message.getType() + " for eventId=" + message.getEventId(), e);
    }
  }
}
