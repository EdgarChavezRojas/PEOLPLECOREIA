package com.solveria.TimeAndBearings.domain.model.ar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.solveria.TimeAndBearings.domain.event.SecurityPunchIncidentEvent;
import com.solveria.TimeAndBearings.domain.exception.DuplicateBiometricEnrollmentException;
import com.solveria.TimeAndBearings.domain.exception.InvalidDeviceSignatureException;
import com.solveria.TimeAndBearings.domain.model.enums.AuthMethod;
import com.solveria.TimeAndBearings.domain.model.enums.AuthResult;
import com.solveria.TimeAndBearings.domain.model.enums.BiometricType;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceRole;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClockingDeviceTest {

    @Test
    void shouldThrowExceptionWhenKioskHasNoSignature() {
        // given
        ClockingDevice device = ClockingDevice.register(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SN-001",
                DeviceType.KIOSK,
                DeviceRole.PRIMARY,
                new DeviceCapabilities(true, false, false, false, "1.0.0", "public-key"),
                LocalDateTime.of(2025, 1, 1, 8, 0),
                "admin");

        // when
        // then
        assertThatThrownBy(() -> device.validateSignature(" "))
                .isInstanceOf(InvalidDeviceSignatureException.class)
                .hasMessageContaining("Device Signature Integrity");
    }

    @Test
    void shouldThrowExceptionWhenDuplicateFingerprintEnrollmentForSameUser() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 8, 0);
        DeviceCapabilities capabilities = DeviceCapabilities.biometricReader("1.0.0", "public-key");
        ClockingDevice device = ClockingDevice.register(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SN-002",
                DeviceType.BIOMETRIC_READER,
                DeviceRole.PRIMARY,
                capabilities,
                now,
                "admin");
        device.activate(capabilities, now.plusMinutes(1), "admin");
        UUID relationshipId = UUID.randomUUID();
        device.enrollBiometric(
                relationshipId,
                BiometricType.FINGERPRINT,
                "hash-1",
                BigDecimal.ONE,
                now.plusMinutes(2));

        // when
        // then
        assertThatThrownBy(() -> device.enrollBiometric(
                relationshipId,
                BiometricType.FINGERPRINT,
                "hash-2",
                BigDecimal.ONE,
                now.plusMinutes(3)))
                .isInstanceOf(DuplicateBiometricEnrollmentException.class)
                .hasMessageContaining("already has an ACTIVE");
    }

    @Test
    void shouldCreateIncidentEventWhenFraudDetectedAndNotBlock() {
        // given
        ClockingDevice device = ClockingDevice.register(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SN-003",
                DeviceType.KIOSK,
                DeviceRole.PRIMARY,
                new DeviceCapabilities(true, false, false, false, "1.0.0", "public-key"),
                LocalDateTime.of(2025, 1, 1, 8, 0),
                "admin");

        // when
        device.recordAuthAttempt(
                UUID.randomUUID(),
                AuthMethod.FINGERPRINT,
                AuthResult.FRAUD_DETECTED,
                LocalDateTime.of(2025, 1, 1, 8, 5));
        List<?> events = device.pullDomainEvents();

        // then
        assertThat(device.getPunchAttemptLogs()).hasSize(1);
        assertThat(events)
                .anyMatch(event -> event instanceof SecurityPunchIncidentEvent);
    }
}

