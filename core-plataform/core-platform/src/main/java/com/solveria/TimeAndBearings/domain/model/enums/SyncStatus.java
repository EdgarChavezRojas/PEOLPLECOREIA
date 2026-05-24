package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Estado de sincronización del dispositivo con el servidor. Diccionario de Datos BC-TM v1.2 –
 * DeviceHeartbeat.sync_status.
 */
public enum SyncStatus {

  /** El dispositivo tiene la lista de enrollments actualizada. */
  SYNCED,

  /** Hay cambios pendientes de sincronización (enrollments nuevos o revocados). */
  PENDING_SYNC,

  /** El dispositivo no ha confirmado la sincronización dentro del umbral esperado. */
  OUT_OF_SYNC
}
