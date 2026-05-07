package com.solveria.TimeAndBearings.application.port.inbound;

import com.solveria.TimeAndBearings.application.usecase.DeviceLifecycleUseCase;
import com.solveria.TimeAndBearings.domain.model.enums.BiometricType;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceRole;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.domain.model.enums.RevocationReason;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inbound Port: DeviceLifecyclePort.
 * Define los casos de uso de gestión del ciclo de vida de dispositivos (WF-TM04).
 *
 * <p>Implementado por {@link DeviceLifecycleUseCase}.
 * Llamado desde controladores REST o mensajes de entrada.
 */
public interface DeviceLifecyclePort {

    /**
     * Registra un nuevo ClockingDevice en estado PROVISIONING (WF-TM04 paso 1).
     * Enforces la Invariante de Unicidad de Dispositivo Primario antes de persistir.
     *
     * @return Command result con el UUID del nuevo dispositivo.
     */
    RegisterDeviceResult registerDevice(RegisterDeviceCommand command);

    /**
     * Activa el dispositivo e instala la clave pública del par criptográfico (WF-TM04 paso 2).
     */
    void activateDevice(ActivateDeviceCommand command);

    /**
     * Suspende el dispositivo temporalmente.
     */
    void suspendDevice(SuspendDeviceCommand command);

    /**
     * Da de baja definitiva el dispositivo y revoca todos sus enrollments (WF-TM04 Flujo Baja).
     */
    void decommissionDevice(DecommissionDeviceCommand command);

    /**
     * Enrola el template biométrico de un colaborador en un dispositivo (WF-TM04 Enrolamiento paso 2-3).
     * Enforces que no exista un enrollment ACTIVE previo del mismo tipo (P-TM29).
     *
     * @return UUID del BiometricEnrollment creado.
     */
    UUID enrollBiometric(EnrollBiometricCommand command);

    /**
     * Revoca el enrollment biométrico de un collaborador. Trigger manual (EMPLOYEE_REQUEST, FRAUD_DETECTED).
     */
    void revokeEnrollment(RevokeEnrollmentCommand command);

    // ── Command Records ───────────────────────────────────────────────────────

    record RegisterDeviceCommand(
            UUID tenantId,
            UUID orgUnitId,
            String serialNumber,
            DeviceType deviceType,
            DeviceRole deviceRole,
            boolean supportsFingerprint,
            boolean supportsFacial,
            boolean supportsNfc,
            boolean supportsQr,
            String firmwareVersion,
            LocalDateTime serverNtpNow,
            String actorId
    ) {}

    record RegisterDeviceResult(UUID deviceId) {}

    record ActivateDeviceCommand(
            UUID deviceId,
            UUID tenantId,
            String publicKeyPem,
            String firmwareVersion,
            LocalDateTime serverNtpNow,
            String actorId
    ) {}

    record SuspendDeviceCommand(
            UUID deviceId,
            UUID tenantId,
            LocalDateTime serverNtpNow,
            String actorId,
            String reason
    ) {}

    record DecommissionDeviceCommand(
            UUID deviceId,
            UUID tenantId,
            LocalDateTime serverNtpNow,
            String actorId
    ) {}

    record EnrollBiometricCommand(
            UUID deviceId,
            UUID tenantId,
            UUID relationshipId,
            BiometricType biometricType,
            String templateHash,
            BigDecimal templateQualityScore,
            LocalDateTime serverNtpNow
    ) {}

    record RevokeEnrollmentCommand(
            UUID deviceId,
            UUID enrollmentId,
            UUID tenantId,
            LocalDateTime serverNtpNow,
            RevocationReason revocationReason
    ) {}
}
