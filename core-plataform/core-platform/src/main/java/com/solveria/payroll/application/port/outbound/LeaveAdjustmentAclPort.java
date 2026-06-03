package com.solveria.payroll.application.port.outbound;

import java.util.UUID;

/**
 * Puerto de ACL (Anti-Corruption Layer) para resolver dependencias externas de nómina relacionadas
 * con licencias y periodos.
 */
public interface LeaveAdjustmentAclPort {

  /** Obtiene el relationshipId asociado a una transacción de licencia. */
  UUID getRelationshipIdByTransactionId(UUID transactionId);

  /** Obtiene el ID del último periodo de nómina activo/válido del tenant. */
  UUID getLatestPeriodId(UUID tenantId);
}
