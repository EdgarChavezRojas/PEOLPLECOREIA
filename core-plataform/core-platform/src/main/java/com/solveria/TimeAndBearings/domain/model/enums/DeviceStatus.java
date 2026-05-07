package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Estado del ciclo de vida de un ClockingDevice.
 * Diccionario de Datos BC-TM v1.2 – ClockingDevice.status.
 * WF-TM04: Gestión de Dispositivos y Biometría.
 */
public enum DeviceStatus {

    /** El dispositivo fue registrado pero aún no recibió su par de claves criptográficas (WF-TM04 paso 1-2). */
    PROVISIONING,

    /** El dispositivo está operativo y acepta marcaciones. */
    ACTIVE,

    /** El dispositivo fue suspendido temporalmente (pérdida, mantenimiento). No acepta marcaciones. */
    SUSPENDED,

    /** El dispositivo fue dado de baja definitivamente (WF-TM04 Flujo Baja/Revocación). */
    DECOMMISSIONED
}
