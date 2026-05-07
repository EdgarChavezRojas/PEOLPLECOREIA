package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Excepción lanzada cuando un TimeEntry proveniente de KIOSK o BIOMETRIC_READER
 * llega sin firma digital válida, violando la Invariante de Firma Digital
 * (Device Signature Integrity – BC-TM v1.2 – Aggregate 15).
 */
public class InvalidDeviceSignatureException extends DomainException {

    public InvalidDeviceSignatureException(UUID deviceId) {
        super(
                "DEVICE_SIGNATURE_INVALID",
                Map.of("deviceId", deviceId.toString()),
                "TimeEntry from device [" + deviceId +
                "] has an invalid or missing device signature (Device Signature Integrity Invariant)."
        );
    }
}
