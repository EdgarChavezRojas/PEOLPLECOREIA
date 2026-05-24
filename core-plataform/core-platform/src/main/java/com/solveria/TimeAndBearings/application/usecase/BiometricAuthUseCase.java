package com.solveria.TimeAndBearings.application.usecase;

import com.solveria.TimeAndBearings.application.command.RecordHeartbeatCommand;
import com.solveria.TimeAndBearings.application.command.VerifyBiometricAuthCommand;
import com.solveria.TimeAndBearings.application.port.inbound.BiometricAuthPort;
import com.solveria.TimeAndBearings.application.port.outbound.ClockingDeviceRepositoryPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.enums.AuthResult;
import com.solveria.TimeAndBearings.domain.model.enums.EnrollmentStatus;
import com.solveria.TimeAndBearings.domain.model.enums.SyncStatus;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: BiometricAuthUseCase — Autenticación Biométrica y Heartbeat (P-TM29 / P-TM30).
 *
 * <p>Orquesta la verificación de autenticación biométrica durante el flujo de marcación en tiempo
 * real (WF-TM01 paso 3). Aplica:
 *
 * <ul>
 *   <li><b>P-TM29:</b> Niveles de autenticación según canal del dispositivo.
 *   <li><b>P-TM30:</b> Detección de proxy clocking — emite SecurityPunchIncidentEvent.
 *   <li><b>Invariante Device Signature Integrity:</b> Valida firma antes de registrar el intento.
 * </ul>
 *
 * <p><b>Non-Blocking Design:</b> el método retorna el AuthResult en todos los casos. El bloqueo del
 * dispositivo está prohibido por diseño (P-TM30).
 *
 * <p>Implementa {@link BiometricAuthPort} (inbound).
 */
@Service
@Transactional
public class BiometricAuthUseCase implements BiometricAuthPort {

  private final ClockingDeviceRepositoryPort deviceRepository;
  private final EventOutboxPort eventOutbox;

  public BiometricAuthUseCase(
      ClockingDeviceRepositoryPort deviceRepository, EventOutboxPort eventOutbox) {
    this.deviceRepository = deviceRepository;
    this.eventOutbox = eventOutbox;
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Verify Biometric Auth (WF-TM01 paso 3)
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public AuthResult verifyBiometricAuth(VerifyBiometricAuthCommand command) {
    ClockingDevice device = loadDeviceOrThrow(command.deviceId(), command.tenantId());

    // Invariante Device Signature Integrity — lanza InvalidDeviceSignatureException si inválida
    device.validateSignature(command.deviceSignature());

    // Determine auth result: check if relationship has an ACTIVE enrollment for the auth method
    // type
    // The actual biometric matching is done in hardware/firmware; here we validate credential
    // state.
    AuthResult result = evaluateCredentialState(device, command);

    // Record attempt (includes consecutive failure tracking and fraud event emission — P-TM30)
    var attemptLog =
        device.recordAuthAttempt(
            command.relationshipId(), command.authMethod(), result, command.serverNtpNow());

    deviceRepository.save(device);
    publishEvents(device);

    return result;
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Record Heartbeat
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public void recordDeviceHeartbeat(RecordHeartbeatCommand command) {
    ClockingDevice device = loadDeviceOrThrow(command.deviceId(), command.tenantId());

    SyncStatus syncStatus;
    try {
      syncStatus = SyncStatus.valueOf(command.syncStatusName());
    } catch (IllegalArgumentException e) {
      syncStatus = SyncStatus.OUT_OF_SYNC;
    }

    device.recordHeartbeat(
        command.serverNtpNow(), syncStatus, command.batteryLevel(), command.enrolledCount());

    deviceRepository.save(device);
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Internal helpers
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Evaluates the credential state of the relationship on the device. Returns REVOKED_CREDENTIAL if
   * the enrollment is REVOKED (triggers security incident — P-TM30). Returns SUCCESS if an ACTIVE
   * enrollment exists. The actual biometric matching result (BIOMETRIC_FAIL) comes from the device
   * hardware and is passed directly; this method only checks the credential lifecycle state.
   */
  private AuthResult evaluateCredentialState(
      ClockingDevice device, VerifyBiometricAuthCommand command) {
    boolean hasRevokedEnrollment =
        device.getEnrollments().stream()
            .filter(e -> e.getRelationshipId().equals(command.relationshipId()))
            .anyMatch(e -> e.getStatus() == EnrollmentStatus.REVOKED);

    if (hasRevokedEnrollment) {
      return AuthResult.REVOKED_CREDENTIAL;
    }

    boolean hasActiveEnrollment =
        device.getEnrollments().stream()
            .filter(e -> e.getRelationshipId().equals(command.relationshipId()))
            .anyMatch(e -> e.getStatus() == EnrollmentStatus.ACTIVE);

    // If no active enrollment exists and the device is physical — treat as BIOMETRIC_FAIL
    return hasActiveEnrollment ? AuthResult.SUCCESS : AuthResult.BIOMETRIC_FAIL;
  }

  private ClockingDevice loadDeviceOrThrow(UUID deviceId, UUID tenantId) {
    return deviceRepository
        .findByDeviceId(deviceId, tenantId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "ClockingDevice not found: deviceId=" + deviceId + " tenantId=" + tenantId));
  }

  private void publishEvents(ClockingDevice device) {
    List<DomainEvent> events = device.pullDomainEvents();
    eventOutbox.store(events);
  }
}
