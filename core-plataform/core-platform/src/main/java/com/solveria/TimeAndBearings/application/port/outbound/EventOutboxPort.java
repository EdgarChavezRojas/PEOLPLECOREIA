package com.solveria.TimeAndBearings.application.port.outbound;

import com.solveria.core.shared.events.DomainEvent;
import java.util.List;

/**
 * Outbound Port: Transactional Outbox for domain events.
 *
 * <p>Guarantees at-least-once delivery of domain events to the Message Broker
 * (Kafka / any broker) within the same DB transaction as the aggregate save.
 * The infrastructure adapter stores events in an {@code outbox} table and a
 * background relay process publishes them to the broker.
 *
 * <p>Events published by BC-TM:
 * <ul>
 *   <li>{@code PUNCH_ANOMALY_DETECTED} → BC-01 Core HR (ESS/MSS Notifications).</li>
 *   <li>{@code EXCEPTION_AUTO_CLOSED}  → BC-01 Core HR (ESS/MSS Notifications).</li>
 * </ul>
 */
public interface EventOutboxPort {

    /**
     * Stores a list of domain events in the transactional outbox table.
     * Must be called within the same transaction as the aggregate save.
     *
     * @param events Domain events to store. No-op if list is empty.
     */
    void store(List<DomainEvent> events);
}
