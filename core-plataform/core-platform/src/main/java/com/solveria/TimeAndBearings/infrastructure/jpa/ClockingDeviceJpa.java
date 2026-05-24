package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceRole;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceStatus;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceHeartbeat;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for {@code clocking_device} table — Aggregate 15. Extends {@link BaseEntity}
 * (multi-tenant filter, audit timestamps, optimistic locking).
 *
 * <p>{@link DeviceCapabilities} and {@link DeviceHeartbeat} are mapped as {@code @Embedded} inline
 * within this entity.
 *
 * <p>This class carries NO domain logic. All business behavior lives in {@link ClockingDevice}.
 */
@Entity
@Table(
    name = "clocking_device",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_device_serial_tenant",
            columnNames = {"serial_number", "tenant_id"}))
public class ClockingDeviceJpa extends BaseEntity {
  @Id
  @Column(name = "device_id", nullable = false, updatable = false, columnDefinition = "UUID")
  private UUID deviceId;

  @Column(name = "org_unit_id", nullable = false, columnDefinition = "UUID")
  private UUID orgUnitId;

  @Column(name = "serial_number", nullable = false, length = 100)
  private String serialNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "device_type", nullable = false, length = 30)
  private DeviceType deviceType;

  @Enumerated(EnumType.STRING)
  @Column(name = "device_role", nullable = false, length = 20)
  private DeviceRole deviceRole;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private DeviceStatus status;

  @Column(name = "installed_at")
  private LocalDateTime installedAt;

  @Column(name = "decommissioned_at")
  private LocalDateTime decommissionedAt;

  /** DeviceCapabilities embedded inline — NOT NULL for KIOSK/BIOMETRIC_READER once activated. */
  @Embedded private DeviceCapabilitiesEmbeddable capabilities;

  /** DeviceHeartbeat embedded inline — NULL until first heartbeat received. */
  @Embedded private DeviceHeartbeatEmbeddable heartbeat;

  @OneToMany(
      mappedBy = "device",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<BiometricEnrollmentJpa> enrollments = new ArrayList<>();

  @OneToMany(
      mappedBy = "device",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<PunchAttemptLogJpa> punchAttemptLogs = new ArrayList<>();

  @OneToMany(
      mappedBy = "device",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<DeviceAuditLogJpa> auditLogs = new ArrayList<>();

  public ClockingDeviceJpa() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public UUID getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(UUID id) {
    this.deviceId = id;
  }

  public UUID getOrgUnitId() {
    return orgUnitId;
  }

  public void setOrgUnitId(UUID id) {
    this.orgUnitId = id;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String sn) {
    this.serialNumber = sn;
  }

  public DeviceType getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(DeviceType t) {
    this.deviceType = t;
  }

  public DeviceRole getDeviceRole() {
    return deviceRole;
  }

  public void setDeviceRole(DeviceRole r) {
    this.deviceRole = r;
  }

  public DeviceStatus getStatus() {
    return status;
  }

  public void setStatus(DeviceStatus s) {
    this.status = s;
  }

  public LocalDateTime getInstalledAt() {
    return installedAt;
  }

  public void setInstalledAt(LocalDateTime d) {
    this.installedAt = d;
  }

  public LocalDateTime getDecommissionedAt() {
    return decommissionedAt;
  }

  public void setDecommissionedAt(LocalDateTime d) {
    this.decommissionedAt = d;
  }

  public DeviceCapabilitiesEmbeddable getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(DeviceCapabilitiesEmbeddable c) {
    this.capabilities = c;
  }

  public DeviceHeartbeatEmbeddable getHeartbeat() {
    return heartbeat;
  }

  public void setHeartbeat(DeviceHeartbeatEmbeddable hb) {
    this.heartbeat = hb;
  }

  public List<BiometricEnrollmentJpa> getEnrollments() {
    return enrollments;
  }

  public void setEnrollments(List<BiometricEnrollmentJpa> items) {
    this.enrollments = items;
  }

  public List<PunchAttemptLogJpa> getPunchAttemptLogs() {
    return punchAttemptLogs;
  }

  public void setPunchAttemptLogs(List<PunchAttemptLogJpa> items) {
    this.punchAttemptLogs = items;
  }

  public List<DeviceAuditLogJpa> getAuditLogs() {
    return auditLogs;
  }

  public void setAuditLogs(List<DeviceAuditLogJpa> items) {
    this.auditLogs = items;
  }
}
