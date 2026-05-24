package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.application.command.SyncBankAccountCommand;
import com.solveria.core.financial.application.port.DispersionPort;
import com.solveria.core.financial.domain.event.BankAccountSyncedEvent;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Sincronizar cuenta bancaria y desbloquear archivo de dispersión. Evento:
 * BANK_ACCOUNT_SYNCED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBankAccountUseCase implements DispersionPort {

  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public void syncBankAccount(
      UUID personId, String bankAccountNumber, String bankCode, UUID tenantId, String userId) {
    log.info("event=SYNC_BANK_ACCOUNT personId={} bankCode={}", personId, bankCode);

    SyncBankAccountCommand cmd =
        new SyncBankAccountCommand(personId, bankAccountNumber, bankCode, tenantId, userId);

    BankAccountSyncedEvent event =
        new BankAccountSyncedEvent(cmd.personId(), cmd.bankAccountNumber(), cmd.bankCode());

    log.info("event=BANK_ACCOUNT_SYNCED personId={}", personId);
  }
}
