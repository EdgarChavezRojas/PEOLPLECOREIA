package com.solveria.TimeAndBearings.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solveria.TimeAndBearings.application.command.DecommissionDeviceCommand;
import com.solveria.TimeAndBearings.application.port.outbound.ClockingDeviceRepositoryPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.domain.event.BiometricEnrollmentRevokedEvent;
import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.enums.BiometricType;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceRole;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceStatus;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceLifecycleUseCaseTest {

  @Mock private ClockingDeviceRepositoryPort deviceRepository;

  @Mock private EventOutboxPort eventOutbox;

  @Test
  void shouldDecommissionDeviceAndRevokeAllActiveEnrollmentsPerWorkflowWFTM04() {
    // given
    DeviceLifecycleUseCase useCase = new DeviceLifecycleUseCase(deviceRepository, eventOutbox);
    UUID tenantId = UUID.randomUUID();
    UUID orgUnitId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    ClockingDevice device =
        ClockingDevice.register(
            tenantId,
            orgUnitId,
            "SN-DEV-001",
            DeviceType.BIOMETRIC_READER,
            DeviceRole.PRIMARY,
            new DeviceCapabilities(true, true, false, false, "1.0.0", null),
            now,
            "admin");

    device.activate(
        new DeviceCapabilities(true, true, false, false, "1.0.1", "PUBLIC_KEY_PEM"),
        now.plusMinutes(5),
        "admin");

    device.enrollBiometric(
        UUID.randomUUID(),
        BiometricType.FINGERPRINT,
        "hash-1",
        new BigDecimal("0.95"),
        now.plusMinutes(10));

    device.enrollBiometric(
        UUID.randomUUID(),
        BiometricType.FACIAL,
        "hash-2",
        new BigDecimal("0.93"),
        now.plusMinutes(11));

    when(deviceRepository.findByDeviceId(eq(device.getDeviceId()), eq(tenantId)))
        .thenReturn(Optional.of(device));

    DecommissionDeviceCommand command =
        new DecommissionDeviceCommand(device.getDeviceId(), tenantId, now.plusMinutes(20), "admin");

    // when
    useCase.decommissionDevice(command);

    // then
    assertThat(device.getStatus()).isEqualTo(DeviceStatus.DECOMMISSIONED);
    verify(deviceRepository).save(device);

    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventOutbox).store(eventsCaptor.capture());
    long revokedEvents =
        eventsCaptor.getValue().stream()
            .filter(event -> event instanceof BiometricEnrollmentRevokedEvent)
            .count();
    assertThat(revokedEvents).isEqualTo(2);
  }
}
