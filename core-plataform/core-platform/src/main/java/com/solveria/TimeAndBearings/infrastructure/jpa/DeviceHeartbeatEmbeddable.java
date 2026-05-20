package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.TimeAndBearings.domain.model.enums.SyncStatus;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceHeartbeat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

/**
 * Embeddable for {@link DeviceHeartbeat}. Mapped inline within {@link ClockingDeviceJpa}. Puro JPA
 * — sin lógica de dominio.
 */
@Embeddable
public class DeviceHeartbeatEmbeddable {

  @Column(name = "last_seen_at")
  private LocalDateTime lastSeenAt;

  /** 0-100. NULL for kioscos with AC power. */
  @Column(name = "battery_level")
  private Integer batteryLevel;

  @Enumerated(EnumType.STRING)
  @Column(name = "sync_status", length = 20)
  private SyncStatus syncStatus;

  @Column(name = "enrolled_employees_count")
  private Integer enrolledEmployeesCount;

  public DeviceHeartbeatEmbeddable() {}

  public LocalDateTime getLastSeenAt() {
    return lastSeenAt;
  }

  public void setLastSeenAt(LocalDateTime d) {
    this.lastSeenAt = d;
  }

  public Integer getBatteryLevel() {
    return batteryLevel;
  }

  public void setBatteryLevel(Integer v) {
    this.batteryLevel = v;
  }

  public SyncStatus getSyncStatus() {
    return syncStatus;
  }

  public void setSyncStatus(SyncStatus s) {
    this.syncStatus = s;
  }

  public Integer getEnrolledEmployeesCount() {
    return enrolledEmployeesCount;
  }

  public void setEnrolledEmployeesCount(Integer v) {
    this.enrolledEmployeesCount = v;
  }
}
