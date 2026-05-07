package com.solveria.core.financial.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Primary Port: Budget Allocation & Funding Control. Expone operaciones de gestión presupuestaria y
 * verificación de fondos.
 */
public interface BudgetAllocationPort {

  /**
   * W1: Valida que un FundingSource tenga saldo suficiente para un costo laboral.
   *
   * @return true si hay fondos suficientes
   */
  boolean validateFundingSource(UUID sourceId, BigDecimal requiredAmount, String approverUserId);

  /** Crea un nuevo FundingSource (proyecto). */
  UUID createFundingSource(
      String projectCode, BigDecimal totalBudget, String tenantId, String createdBy);

  /** W2: Ajusta la distribución del costo laboral (LaborCostSplit). Invariante: suma = 100%. */
  void adjustCostSplit(
      UUID sourceId,
      java.util.List<
              com.solveria.core.financial.application.command.AdjustCostSplitCommand.SplitEntry>
          splits,
      String approverUserId);

  /** Asigna presupuesto (reduce saldo disponible). */
  void allocateBudget(UUID sourceId, BigDecimal amount);
}
