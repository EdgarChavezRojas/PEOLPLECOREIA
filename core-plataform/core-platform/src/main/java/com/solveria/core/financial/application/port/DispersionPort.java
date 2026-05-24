package com.solveria.core.financial.application.port;

import java.util.UUID;

/**
 * Primary Port: Dispersion (sincronización bancaria). Expone operaciones de sincronización de
 * cuentas bancarias y generación de archivos de dispersión.
 */
public interface DispersionPort {

  /**
   * Sincroniza una cuenta bancaria para un trabajador. Evento: BANK_ACCOUNT_SYNCED (desbloquea
   * archivo de dispersión).
   */
  void syncBankAccount(
      UUID personId, String bankAccountNumber, String bankCode, UUID tenantId, String userId);
}
