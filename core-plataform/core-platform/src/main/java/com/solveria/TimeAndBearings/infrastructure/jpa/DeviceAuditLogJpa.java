package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for {@code device_audit_log} table — Aggregate 15. Extends {@link BaseEntity}.
 * Append-only — immutable after creation.
 *
 * <p>Registra todos los eventos del ciclo de vida del ClockingDevice: PROVISIONED, ACTIVATED,
 * SUSPENDED, DECOMMISSIONED, BIOMETRIC_ENROLLED, BIOMETRIC_REVOKED_BULK.
 */
@Entity
@Table(name = "device_audit_log")
public class DeviceAuditLogJpa extends BaseEntity {


  @Column(name = "audit_log_id", nullable = false, updatable = false, columnDefinition = "UUID")
  private UUID auditLogId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "device_id", nullable = false, updatable = false)
  private ClockingDeviceJpa device;

  @Column(name = "event_type", nullable = false, length = 50)
  private String eventType;

  /** UUID del actor o "SYSTEM" para eventos automáticos. */
  @Column(name = "actor_id", length = 100)
  private String actorId;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private LocalDateTime occurredAt;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  public DeviceAuditLogJpa() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public UUID getAuditLogId() {
    return auditLogId;
  }

  public void setAuditLogId(UUID id) {
    this.auditLogId = id;
  }

  public ClockingDeviceJpa getDevice() {
    return device;
  }

  public void setDevice(ClockingDeviceJpa d) {
    this.device = d;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String e) {
    this.eventType = e;
  }

  public String getActorId() {
    return actorId;
  }

  public void setActorId(String a) {
    this.actorId = a;
  }

  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(LocalDateTime d) {
    this.occurredAt = d;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String d) {
    this.description = d;
  }
}
