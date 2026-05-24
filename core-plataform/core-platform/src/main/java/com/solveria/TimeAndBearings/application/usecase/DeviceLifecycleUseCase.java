package com.solveria.TimeAndBearings.application.usecase;

import com.solveria.TimeAndBearings.application.command.ActivateDeviceCommand;
import com.solveria.TimeAndBearings.application.command.DecommissionDeviceCommand;
import com.solveria.TimeAndBearings.application.command.EnrollBiometricCommand;
import com.solveria.TimeAndBearings.application.command.RegisterDeviceCommand;
import com.solveria.TimeAndBearings.application.command.RevokeEnrollmentCommand;
import com.solveria.TimeAndBearings.application.command.SuspendDeviceCommand;
import com.solveria.TimeAndBearings.application.port.inbound.DeviceLifecyclePort;
import com.solveria.TimeAndBearings.application.port.outbound.ClockingDeviceRepositoryPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.domain.exception.DuplicatePrimaryDeviceException;
import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceRole;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: DeviceLifecycleUseCase — Gestión del Ciclo de Vida de Dispositivos (WF-TM04).
 *
 * <p>Orquesta: registro, activación, suspensión, decomisión y enrolamiento biométrico. Enforces la
 * Invariante de Unicidad de Dispositivo Primario ANTES de invocar el AR factory. Despacha los
 * domain events al {@link EventOutboxPort} (transactional outbox).
 *
 * <p>Implementa {@link DeviceLifecyclePort} (inbound).
 */
@Service
@Transactional
public class DeviceLifecycleUseCase implements DeviceLifecyclePort {

  private final ClockingDeviceRepositoryPort deviceRepository;
  private final EventOutboxPort eventOutbox;

  public DeviceLifecycleUseCase(
      ClockingDeviceRepositoryPort deviceRepository, EventOutboxPort eventOutbox) {
    this.deviceRepository = deviceRepository;
    this.eventOutbox = eventOutbox;
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Register Device (WF-TM04 paso 1)
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public RegisterDeviceResult registerDevice(RegisterDeviceCommand command) {

    // Enforce Device Uniqueness Invariant BEFORE calling the AR factory
    if (command.deviceRole() == DeviceRole.PRIMARY) {
      boolean duplicatePrimary =
          deviceRepository.existsActivePrimaryDevice(
              command.orgUnitId(), command.deviceType(), command.tenantId());
      if (duplicatePrimary) {
        throw new DuplicatePrimaryDeviceException(command.orgUnitId(), command.deviceType().name());
      }
    }

    DeviceCapabilities caps =
        new DeviceCapabilities(
            command.supportsFingerprint(),
            command.supportsFacial(),
            command.supportsNfc(),
            command.supportsQr(),
            command.firmwareVersion(),
            null // publicKeyPem is null until activation step (WF-TM04 paso 2)
            );

    ClockingDevice device =
        ClockingDevice.register(
            command.tenantId(),
            command.orgUnitId(),
            command.serialNumber(),
            command.deviceType(),
            command.deviceRole(),
            caps,
            command.serverNtpNow(),
            command.actorId());

    deviceRepository.save(device);
    publishEvents(device);

    return new RegisterDeviceResult(device.getDeviceId());
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Activate Device (WF-TM04 paso 2)
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public void activateDevice(ActivateDeviceCommand command) {
    ClockingDevice device = loadDeviceOrThrow(command.deviceId(), command.tenantId());

    DeviceCapabilities activatedCaps =
        new DeviceCapabilities(
            device.getCapabilities().supportsFingerprint(),
            device.getCapabilities().supportsFacial(),
            device.getCapabilities().supportsNfc(),
            device.getCapabilities().supportsQr(),
            command.firmwareVersion(),
            command.publicKeyPem());

    device.activate(activatedCaps, command.serverNtpNow(), command.actorId());
    deviceRepository.save(device);
    publishEvents(device);
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Suspend Device
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public void suspendDevice(SuspendDeviceCommand command) {
    ClockingDevice device = loadDeviceOrThrow(command.deviceId(), command.tenantId());
    device.suspend(command.serverNtpNow(), command.actorId(), command.reason());
    deviceRepository.save(device);
    publishEvents(device);
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Decommission Device (WF-TM04 Flujo Baja)
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public void decommissionDevice(DecommissionDeviceCommand command) {
    ClockingDevice device = loadDeviceOrThrow(command.deviceId(), command.tenantId());
    device.decommission(command.serverNtpNow(), command.actorId());
    deviceRepository.save(device);
    publishEvents(device); // includes BiometricEnrollmentRevokedEvent for each enrollment
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Enroll Biometric (WF-TM04 Enrolamiento paso 2-3)
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public UUID enrollBiometric(EnrollBiometricCommand command) {
    ClockingDevice device = loadDeviceOrThrow(command.deviceId(), command.tenantId());

    // AR enforces DuplicateBiometricEnrollmentException internally
    var enrollment =
        device.enrollBiometric(
            command.relationshipId(),
            command.biometricType(),
            command.templateHash(),
            command.templateQualityScore(),
            command.serverNtpNow());

    deviceRepository.save(device);
    publishEvents(device);

    return enrollment.getEnrollmentId();
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Revoke Enrollment (WF-TM04 Flujo Baja paso 2 — manual)
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public void revokeEnrollment(RevokeEnrollmentCommand command) {
    ClockingDevice device = loadDeviceOrThrow(command.deviceId(), command.tenantId());
    device.revokeEnrollment(
        command.enrollmentId(), command.serverNtpNow(), command.revocationReason());
    deviceRepository.save(device);
    publishEvents(device);
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Internal helpers
  // ─────────────────────────────────────────────────────────────────────────

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
