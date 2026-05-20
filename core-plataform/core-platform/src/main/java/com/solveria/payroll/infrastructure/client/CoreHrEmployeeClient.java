package com.solveria.payroll.infrastructure.client;

import java.util.UUID;

public interface CoreHrEmployeeClient {
  boolean hasSyncedBankAccount(UUID employeeId, UUID tenantId);
}
