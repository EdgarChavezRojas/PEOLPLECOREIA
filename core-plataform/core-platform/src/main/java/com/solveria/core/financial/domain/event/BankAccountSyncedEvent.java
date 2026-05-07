package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Actualiza el destino de transferencia bancaria y desbloquea el archivo de
 * dispersión.
 */
public record BankAccountSyncedEvent(
    UUID personId, String bankAccountNumber, String bankCode, Instant occurredAt)
    implements DomainEvent {

  public BankAccountSyncedEvent(UUID personId, String bankAccountNumber, String bankCode) {
    this(personId, bankAccountNumber, bankCode, Instant.now());
  }
}
