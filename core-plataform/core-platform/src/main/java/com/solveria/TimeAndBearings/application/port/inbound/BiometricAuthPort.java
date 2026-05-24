package com.solveria.TimeAndBearings.application.port.inbound;

import com.solveria.TimeAndBearings.application.command.RecordHeartbeatCommand;
import com.solveria.TimeAndBearings.application.command.VerifyBiometricAuthCommand;
import com.solveria.TimeAndBearings.application.usecase.BiometricAuthUseCase;
import com.solveria.TimeAndBearings.domain.model.enums.AuthResult;

/**
 * Inbound Port: BiometricAuthPort. Define los casos de uso de autenticación biométrica durante el
 * flujo de marcación (P-TM29 / P-TM30).
 *
 * <p>Implementado por {@link BiometricAuthUseCase}. Invocado desde la capa de marcación en tiempo
 * real (WF-TM01 paso 3) o el dispositivo físico.
 */
public interface BiometricAuthPort {

  /**
   * Verifica la autenticación biométrica de un colaborador en un dispositivo (P-TM29). Registra el
   * intento en PunchAttemptLog y emite SecurityPunchIncidentEvent si corresponde (P-TM30).
   *
   * <p><b>Non-Blocking:</b> incluso en caso de fraude detectado, el método retorna el resultado sin
   * bloquear el dispositivo. El UC persiste el log y emite el evento asincrónicamente.
   *
   * @return Resultado de la autenticación (ver AuthResult).
   */
  AuthResult verifyBiometricAuth(VerifyBiometricAuthCommand command);

  /** Registra un heartbeat del dispositivo y actualiza el estado de sincronización. */
  void recordDeviceHeartbeat(RecordHeartbeatCommand command);
}
