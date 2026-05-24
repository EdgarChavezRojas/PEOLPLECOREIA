package com.solveria.TimeAndBearings.domain.model.vo;

import com.solveria.TimeAndBearings.domain.model.enums.SyncStatus;
import java.time.LocalDateTime;

/**
 * Value Object: latido de estado del dispositivo. Diccionario de Datos BC-TM v1.2 – Agregado 15,
 * DeviceHeartbeat.
 *
 * <p>Puro Java 21 Record — ninguna anotación Spring/JPA. Actualizado cada vez que el dispositivo
 * hace check-in con el servidor. Diseño Non-Blocking: el dispositivo continúa operativo
 * independientemente del estado de sincronización.
 *
 * @param lastSeenAt Último latido recibido del dispositivo (hora del servidor NTP).
 * @param batteryLevel Nivel de batería (0-100). NULL para kioscos con corriente alterna.
 * @param syncStatus Estado de sincronización de la lista de enrollments.
 * @param enrolledEmployeesCount Número de empleados con templates biométricos activos en el
 *     dispositivo.
 */
public record DeviceHeartbeat(
    LocalDateTime lastSeenAt,
    Integer batteryLevel,
    SyncStatus syncStatus,
    int enrolledEmployeesCount) {

  public DeviceHeartbeat {
    if (lastSeenAt == null) {
      throw new IllegalArgumentException("DeviceHeartbeat.lastSeenAt cannot be null.");
    }
    if (syncStatus == null) {
      throw new IllegalArgumentException("DeviceHeartbeat.syncStatus cannot be null.");
    }
    if (batteryLevel != null && (batteryLevel < 0 || batteryLevel > 100)) {
      throw new IllegalArgumentException(
          "DeviceHeartbeat.batteryLevel must be in range [0, 100]. Got: " + batteryLevel);
    }
    if (enrolledEmployeesCount < 0) {
      throw new IllegalArgumentException(
          "DeviceHeartbeat.enrolledEmployeesCount cannot be negative.");
    }
  }

  /** Factory para un primer heartbeat al activar el dispositivo. */
  public static DeviceHeartbeat initial(LocalDateTime activatedAt) {
    return new DeviceHeartbeat(activatedAt, null, SyncStatus.PENDING_SYNC, 0);
  }

  /** Nuevo heartbeat registrando un cambio de estado de sincronización. */
  public DeviceHeartbeat withSync(LocalDateTime now, SyncStatus newSyncStatus, int enrolledCount) {
    return new DeviceHeartbeat(now, this.batteryLevel, newSyncStatus, enrolledCount);
  }
}
