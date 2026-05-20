package com.solveria.TimeAndBearings.domain.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity: DeviceAuditLog — registro inmutable de eventos del ciclo de vida de un ClockingDevice.
 * Diccionario de Datos BC-TM v1.2 – Agregado 15.
 *
 * <p>Inmutable por diseño (append-only). Cubre: provisioning, activación, sincronización,
 * heartbeats, suspensión, decomisión y revocaciones masivas de biometría.
 *
 * <p>Puro Java 21 — ninguna anotación Spring/JPA.
 */
public class DeviceAuditLog {

  private UUID auditLogId;
  private UUID deviceId;
  private String eventType;
  private String actorId;
  private LocalDateTime occurredAt;
  private String description;

  public void setAuditLogId(UUID auditLogId) {
    this.auditLogId = auditLogId;
  }

  public void setDeviceId(UUID deviceId) {
    this.deviceId = deviceId;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public void setActorId(String actorId) {
    this.actorId = actorId;
  }

  public void setOccurredAt(LocalDateTime occurredAt) {
    this.occurredAt = occurredAt;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public DeviceAuditLog(
      UUID auditLogId,
      UUID deviceId,
      String eventType,
      String actorId,
      LocalDateTime occurredAt,
      String description) {

    if (eventType == null || eventType.isBlank()) {
      throw new IllegalArgumentException("DeviceAuditLog.eventType cannot be null or blank.");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("DeviceAuditLog.occurredAt cannot be null.");
    }
    this.auditLogId = auditLogId;
    this.deviceId = deviceId;
    this.eventType = eventType;
    this.actorId = actorId;
    this.occurredAt = occurredAt;
    this.description = description;
  }

  // ── Getters (inmutable) ──────────────────────────────────────────────────

  public UUID getAuditLogId() {
    return auditLogId;
  }

  public UUID getDeviceId() {
    return deviceId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getActorId() {
    return actorId;
  }

  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  public String getDescription() {
    return description;
  }
}
