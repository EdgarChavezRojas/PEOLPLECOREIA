package com.solveria.core.financial.application.command;

import java.util.UUID;

/** Command: Sincronizar cuenta bancaria para dispersión. */
public record SyncBankAccountCommand(
    UUID personId, String bankAccountNumber, String bankCode, UUID tenantId, String userId) {
  public SyncBankAccountCommand {
    if (personId == null) {
      throw new IllegalArgumentException("personId es obligatorio");
    }
    if (bankAccountNumber == null || bankAccountNumber.isBlank()) {
      throw new IllegalArgumentException("bankAccountNumber es obligatorio");
    }
    if (bankCode == null || bankCode.isBlank()) {
      throw new IllegalArgumentException("bankCode es obligatorio");
    }
  }
}
