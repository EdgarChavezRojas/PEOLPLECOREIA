package com.solveria.core.shared.outbox.infrastructure.jpa;

import com.solveria.core.shared.outbox.domain.OutboxState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shared_event_outbox")
@Builder
public class SharedOutboxMessageJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "aggregate_type", length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "type", length = 100, nullable = false)
    private String type;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20, nullable = false)
    private OutboxState state;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;

    protected SharedOutboxMessageJpaEntity() {
    }

    public SharedOutboxMessageJpaEntity(
            UUID eventId,
            String aggregateType,
            UUID aggregateId,
            String type,
            String payload,
            OutboxState state,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            String errorLog) {
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.state = state;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.errorLog = errorLog;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxState getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public void markProcessed(LocalDateTime processedAt) {
        this.state = OutboxState.PROCESSED;
        this.processedAt = processedAt;
        this.errorLog = null;
    }

    public void markFailed(LocalDateTime processedAt, String errorLog) {
        this.state = OutboxState.FAILED;
        this.processedAt = processedAt;
        this.errorLog = errorLog;
    }
}

