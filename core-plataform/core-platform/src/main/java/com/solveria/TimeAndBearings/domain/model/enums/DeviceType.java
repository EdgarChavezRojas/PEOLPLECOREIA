package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Tipo de hardware o canal de marcación.
 * Diccionario de Datos BC-TM v1.2 – ClockingDevice.device_type.
 * WF-TM04: Registro de Dispositivos.
 */
public enum DeviceType {

    /** Kiosco físico instalado en el local. P-TM29 Nivel 1 (huella dactilar). */
    KIOSK,

    /** Lector biométrico dedicado. Firma digital obligatoria (Invariante Device Signature Integrity). */
    BIOMETRIC_READER,

    /** Terminal NFC para marcación por tarjeta de proximidad. */
    NFC_TERMINAL,

    /** Canal lógico de la App ESS del colaborador (móvil). P-TM29 Nivel 2/3. */
    MOBILE_APP_CHANNEL
}
