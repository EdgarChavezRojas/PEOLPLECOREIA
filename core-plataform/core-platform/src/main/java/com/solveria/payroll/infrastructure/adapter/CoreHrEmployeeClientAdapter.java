package com.solveria.payroll.infrastructure.adapter;

import com.solveria.core.financial.domain.event.BankAccountSyncedEvent;
import com.solveria.core.shared.outbox.infrastructure.repository.SharedOutboxRepository;
import com.solveria.payroll.infrastructure.client.CoreHrEmployeeClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreHrEmployeeClientAdapter implements CoreHrEmployeeClient {

  private static final String BANK_ACCOUNT_SYNCED_EVENT = BankAccountSyncedEvent.class.getName();

  private final SharedOutboxRepository sharedOutboxRepository;

  @Override
  public boolean hasSyncedBankAccount(UUID employeeId, UUID tenantId) {
    if (employeeId == null) {
      return false;
    }
    return sharedOutboxRepository.existsByTypeAndAggregateId(
        BANK_ACCOUNT_SYNCED_EVENT, employeeId);
  }
}

