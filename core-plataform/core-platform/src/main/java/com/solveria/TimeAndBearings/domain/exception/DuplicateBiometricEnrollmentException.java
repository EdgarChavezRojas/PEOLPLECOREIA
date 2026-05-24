package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta enrolar un template biométrico para un (relationship_id,
 * biometric_type) que ya posee un enrollment ACTIVE en el dispositivo. BC-TM v1.2 – Aggregate 15,
 * WF-TM04 paso 1 (Enrolamiento Biométrico).
 */
public class DuplicateBiometricEnrollmentException extends DomainException {

  public DuplicateBiometricEnrollmentException(
      UUID deviceId, UUID relationshipId, String biometricType) {
    super(
        "BIOMETRIC_ENROLLMENT_DUPLICATE",
        Map.of(
            "deviceId", deviceId.toString(),
            "relationshipId", relationshipId.toString(),
            "biometricType", biometricType),
        "RelationshipId ["
            + relationshipId
            + "] already has an ACTIVE "
            + biometricType
            + " enrollment on device ["
            + deviceId
            + "].");
  }
}
