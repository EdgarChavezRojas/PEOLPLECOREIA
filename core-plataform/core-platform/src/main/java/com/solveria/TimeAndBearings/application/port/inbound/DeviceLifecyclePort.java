package com.solveria.TimeAndBearings.application.port.inbound;

import com.solveria.TimeAndBearings.application.command.ActivateDeviceCommand;
import com.solveria.TimeAndBearings.application.command.DecommissionDeviceCommand;
import com.solveria.TimeAndBearings.application.command.EnrollBiometricCommand;
import com.solveria.TimeAndBearings.application.command.RegisterDeviceCommand;
import com.solveria.TimeAndBearings.application.command.RevokeEnrollmentCommand;
import com.solveria.TimeAndBearings.application.command.SuspendDeviceCommand;
import com.solveria.TimeAndBearings.application.usecase.DeviceLifecycleUseCase;
import java.util.UUID;

/**
 * Inbound Port: DeviceLifecyclePort. Define los casos de uso de gestión del ciclo de vida de
 * dispositivos (WF-TM04).
 *
 * <p>Implementado por {@link DeviceLifecycleUseCase}. Llamado desde controladores REST o mensajes
 * de entrada.
 */
public interface DeviceLifecyclePort {

  /**
   * Registra un nuevo ClockingDevice en estado PROVISIONING (WF-TM04 paso 1). Enforces la
   * Invariante de Unicidad de Dispositivo Primario antes de persistir.
   *
   * @return Command result con el UUID del nuevo dispositivo.
   */
  RegisterDeviceResult registerDevice(RegisterDeviceCommand command);

  /** Activa el dispositivo e instala la clave pública del par criptográfico (WF-TM04 paso 2). */
  void activateDevice(ActivateDeviceCommand command);

  /** Suspende el dispositivo temporalmente. */
  void suspendDevice(SuspendDeviceCommand command);

  /** Da de baja definitiva el dispositivo y revoca todos sus enrollments (WF-TM04 Flujo Baja). */
  void decommissionDevice(DecommissionDeviceCommand command);

  /**
   * Enrola el template biométrico de un colaborador en un dispositivo (WF-TM04 Enrolamiento paso
   * 2-3). Enforces que no exista un enrollment ACTIVE previo del mismo tipo (P-TM29).
   *
   * @return UUID del BiometricEnrollment creado.
   */
  UUID enrollBiometric(EnrollBiometricCommand command);

  /**
   * Revoca el enrollment biométrico de un collaborador. Trigger manual (EMPLOYEE_REQUEST,
   * FRAUD_DETECTED).
   */
  void revokeEnrollment(RevokeEnrollmentCommand command);

  record RegisterDeviceResult(UUID deviceId) {}
}
