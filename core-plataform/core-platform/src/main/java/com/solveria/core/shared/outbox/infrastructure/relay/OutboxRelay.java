package com.solveria.core.shared.outbox.infrastructure.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.domain.OutboxState;
import com.solveria.core.shared.outbox.infrastructure.jpa.SharedOutboxMessageJpaEntity;
import com.solveria.core.shared.outbox.infrastructure.repository.SharedOutboxRepository;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final SharedOutboxRepository repository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper mapper;


    public OutboxRelay(
            SharedOutboxRepository repository,
            ApplicationEventPublisher publisher,
            ObjectMapper mapper
            ) {
        this.repository = repository;
        this.publisher = publisher;
        this.mapper = mapper;

    }

    @Scheduled(fixedDelayString = "${shared.outbox.relay.delay-ms:5000}")
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
                DomainEvent event = deserialize(message);
                publisher.publishEvent(event);
                message.markProcessed(processedAt);
            } catch (Exception ex) {
                message.markFailed(processedAt, stackTrace(ex));
                log.warn("Outbox relay failed for eventId={} type={}",
                        message.getEventId(), message.getType(), ex);
            }
            repository.save(message);
        }
    }

    private DomainEvent deserialize(SharedOutboxMessageJpaEntity message) throws Exception {
        Class<? extends DomainEvent> eventClass = Class.forName(message.getType()).asSubclass(DomainEvent.class);
        return mapper.readValue(message.getPayload(), eventClass);
    }

    private String stackTrace(Exception ex) {
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
