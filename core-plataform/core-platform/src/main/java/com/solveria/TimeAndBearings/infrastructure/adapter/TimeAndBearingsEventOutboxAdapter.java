package com.solveria.TimeAndBearings.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.domain.OutboxState;
import com.solveria.core.shared.outbox.infrastructure.jpa.SharedOutboxMessageJpaEntity;
import com.solveria.core.shared.outbox.infrastructure.repository.SharedOutboxRepository;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TimeAndBearingsEventOutboxAdapter implements EventOutboxPort {

    private static final List<String> AGGREGATE_ID_CANDIDATES = List.of(
            "aggregateId",
            "periodId",
            "deviceId",
            "ledgerId",
            "deviationId",
            "timesheetPeriodId",
            "attendancePeriodId");

    private final SharedOutboxRepository repository;
    private final ObjectMapper mapper;

    public TimeAndBearingsEventOutboxAdapter(SharedOutboxRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void store(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (DomainEvent event : events) {
            SharedOutboxMessageJpaEntity message = toOutboxMessage(event, now);
            repository.save(message);
        }
    }

    private SharedOutboxMessageJpaEntity toOutboxMessage(DomainEvent event, LocalDateTime createdAt) {
        UUID eventId = resolveEventId(event).orElseGet(UUID::randomUUID);
        String aggregateType = resolveAggregateType(event);
        UUID aggregateId = resolveAggregateId(event).orElse(null);
        String payload = toJson(event);

        return new SharedOutboxMessageJpaEntity(
                eventId,
                aggregateType,
                aggregateId,
                event.getClass().getName(),
                payload,
                OutboxState.PENDING,
                createdAt,
                null,
                null);
    }

    private Optional<UUID> resolveEventId(DomainEvent event) {
        return invokeUuidAccessor(event, "eventId")
                .or(() -> invokeUuidAccessor(event, "getEventId"));
    }

    private String resolveAggregateType(DomainEvent event) {
        String simpleName = event.getClass().getSimpleName();
        return simpleName.endsWith("Event")
                ? simpleName.substring(0, simpleName.length() - "Event".length())
                : simpleName;
    }

    private Optional<UUID> resolveAggregateId(DomainEvent event) {
        for (String candidate : AGGREGATE_ID_CANDIDATES) {
            Optional<UUID> value = invokeUuidAccessor(event, candidate)
                    .or(() -> invokeUuidAccessor(event, "get" + capitalize(candidate)));
            if (value.isPresent()) {
                return value;
            }
        }

        if (event.getClass().isRecord()) {
            return Arrays.stream(event.getClass().getRecordComponents())
                    .filter(component -> UUID.class.equals(component.getType()))
                    .filter(component -> !isEventOrTenantId(component))
                    .map(component -> invokeRecordComponent(event, component))
                    .flatMap(Optional::stream)
                    .findFirst();
        }

        return Optional.empty();
    }

    private boolean isEventOrTenantId(RecordComponent component) {
        return "eventId".equals(component.getName()) || "tenantId".equals(component.getName());
    }

    private Optional<UUID> invokeRecordComponent(DomainEvent event, RecordComponent component) {
        try {
            Object value = component.getAccessor().invoke(event);
            return Optional.ofNullable((UUID) value);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<UUID> invokeUuidAccessor(DomainEvent event, String methodName) {
        try {
            Method method = event.getClass().getMethod(methodName);
            Object value = method.invoke(event);
            if (value instanceof UUID uuid) {
                return Optional.of(uuid);
            }
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String toJson(DomainEvent event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el evento para outbox", ex);
        }
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}

