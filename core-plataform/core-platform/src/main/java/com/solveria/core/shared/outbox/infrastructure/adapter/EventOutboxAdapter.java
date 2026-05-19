package com.solveria.core.shared.outbox.infrastructure.adapter;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.domain.OutboxState;
import com.solveria.core.shared.outbox.infrastructure.jpa.SharedOutboxMessageJpaEntity;
import com.solveria.core.shared.outbox.infrastructure.repository.SharedOutboxRepository;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class EventOutboxAdapter implements EventOutboxPort {
    private final SharedOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        List<SharedOutboxMessageJpaEntity> outboxEntities = events.stream()
                .map(this::mapToOutboxEntity)
                .toList();

        outboxRepository.saveAll(outboxEntities);
        log.debug("Se guardaron {} eventos en el Outbox", outboxEntities.size());
    }

    private SharedOutboxMessageJpaEntity mapToOutboxEntity(DomainEvent event) {
        try {
            // Extraemos el ID del evento (si lo tiene) o generamos uno nuevo.
            UUID eventId = extractUuidFieldDynamically(event, "eventId");
            if (eventId == null) {
                eventId = UUID.randomUUID();
            }

            // Extraemos el Aggregate ID dinámicamente ignorando metadatos.
            UUID aggregateId = extractUuidFieldDynamically(event, null);

            return SharedOutboxMessageJpaEntity.builder()
                    .eventId(eventId)
                    .aggregateType(extractAggregateType(event))
                    .aggregateId(aggregateId)
                    .type(event.getClass().getName())
                    .payload(objectMapper.writeValueAsString(event))
                    .state(OutboxState.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Fallo la serialización del evento Outbox: {}", event.getClass().getName(), e);
            throw new IllegalStateException("Error al serializar el evento de dominio a JSON", e);
        }
    }

    private String extractAggregateType(DomainEvent event) {
        String className = event.getClass().getSimpleName();
        return className.endsWith("Event")
                ? className.substring(0, className.length() - 5)
                : className;
    }

    /**
     * Extrae un UUID dinámicamente mediante Reflection. Funciona tanto para Java Records como para Clases estándar.
     * * @param event El evento de dominio.
     * @param specificTargetName Si es no nulo, busca un nombre exacto (ej. "eventId").
     * Si es nulo, busca cualquier candidato a AggregateID (terminado en "Id").
     */
    private UUID extractUuidFieldDynamically(DomainEvent event, String specificTargetName) {
        if (event.getClass().isRecord()) {
            for (RecordComponent component : event.getClass().getRecordComponents()) {
                if (component.getType().equals(UUID.class) && isMatch(component.getName(), specificTargetName)) {
                    try {
                        return (UUID) component.getAccessor().invoke(event);
                    } catch (Exception ignored) {
                        // Ignoramos la excepción para seguir buscando
                    }
                }
            }
        } else {
            for (Method method : event.getClass().getMethods()) {
                if (method.getReturnType().equals(UUID.class) && method.getParameterCount() == 0) {
                    String methodName = method.getName();
                    if (methodName.startsWith("get") && isMatch(methodName.substring(3), specificTargetName)) {
                        try {
                            return (UUID) method.invoke(event);
                        } catch (Exception ignored) {
                            // Ignoramos la excepción para seguir buscando
                        }
                    }
                }
            }
        }
        return null; // Si la BD tiene nullable=false en aggregateId, podrías retornar UUID.randomUUID() aquí
    }

    private boolean isMatch(String name, String specificTargetName) {
        String lowerName = name.toLowerCase();

        if (specificTargetName != null) {
            return lowerName.equals(specificTargetName.toLowerCase());
        }

        // Regla heurística para encontrar el Aggregate ID:
        // Debe terminar en "id" pero NO ser campos técnicos estándar.
        return lowerName.endsWith("id")
                && !lowerName.equals("eventid")
                && !lowerName.equals("tenantid")
                && !lowerName.equals("correlationid")
                && !lowerName.equals("causationid");
    }
}
