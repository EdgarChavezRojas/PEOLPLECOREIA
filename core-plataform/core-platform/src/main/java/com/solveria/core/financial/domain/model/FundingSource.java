package com.solveria.core.financial.domain.model;

import com.solveria.core.financial.domain.event.CostCenterSplitAdjustedEvent;
import com.solveria.core.financial.domain.event.FundingSourceProjectExhaustedEvent;
import com.solveria.core.financial.domain.event.FundingSourceValidatedEvent;
import com.solveria.core.financial.domain.model.vo.LaborCostSplit;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root: FundingSource. Representa la fuente del dinero (Proyecto). Sin esto no hay
 * imputación analítica.
 *
 * <p>Invariantes: - Consistencia del 100%: la suma de LaborCostSplit.percentage DEBE ser
 * exactamente 100%. - available_budget nunca puede ser negativo.
 */
public class FundingSource extends DomainRoot {

  private final UUID sourceId;
  private final String projectCode;
  private BigDecimal totalBudget;
  private BigDecimal availableBudget;
  private BigDecimal burnRate;
  private final UUID tenantId;
  private final String createdBy;
  private List<LaborCostSplit> costSplits;

  private FundingSource(
      UUID sourceId,
      String projectCode,
      BigDecimal totalBudget,
      BigDecimal availableBudget,
      BigDecimal burnRate,
      UUID tenantId,
      String createdBy,
      List<LaborCostSplit> costSplits) {
    this.sourceId = sourceId;
    this.projectCode = projectCode;
    this.totalBudget = totalBudget;
    this.availableBudget = availableBudget;
    this.burnRate = burnRate;
    this.tenantId = tenantId;
    this.createdBy = createdBy;
    this.costSplits = costSplits != null ? new ArrayList<>(costSplits) : new ArrayList<>();
  }

  /** Factory: crea un nuevo FundingSource con presupuesto completo. */
  public static FundingSource create(
      String projectCode, BigDecimal totalBudget, UUID tenantId, String createdBy) {
    if (projectCode == null || projectCode.isBlank()) {
      throw new IllegalArgumentException("projectCode es obligatorio para ONGs");
    }
    if (totalBudget == null || totalBudget.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("totalBudget debe ser positivo");
    }
    return new FundingSource(
        UUID.randomUUID(),
        projectCode,
        totalBudget,
        totalBudget,
        BigDecimal.ZERO,
        tenantId,
        createdBy,
        new ArrayList<>());
  }

  /** Factory: rehidrata desde persistencia. */
  public static FundingSource rehydrate(
      UUID sourceId,
      String projectCode,
      BigDecimal totalBudget,
      BigDecimal availableBudget,
      BigDecimal burnRate,
      UUID tenantId,
      String createdBy,
      List<LaborCostSplit> costSplits) {
    return new FundingSource(
        sourceId,
        projectCode,
        totalBudget,
        availableBudget,
        burnRate,
        tenantId,
        createdBy,
        costSplits);
  }

  /**
   * Verifica que el ProjectID tenga saldo para sueldo/cargas antes de aprobar contratos. Evento:
   * FUNDING_SOURCE_VALIDATED (Sync/Bloqueante).
   */
  public boolean checkBudgetSufficiency(BigDecimal requiredAmount) {
    boolean sufficient = this.availableBudget.compareTo(requiredAmount) >= 0;
    if (sufficient) {
      registerEvent(new FundingSourceValidatedEvent(this.sourceId, requiredAmount));
    }
    return sufficient;
  }

  /**
   * Asigna presupuesto a un costo laboral, reduciendo el saldo disponible. Si el saldo llega a 0,
   * dispara FUNDING_SOURCE_PROJECT_EXHAUSTED.
   */
  public void allocateBudget(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Monto de asignación debe ser positivo");
    }
    if (this.availableBudget.compareTo(amount) < 0) {
      throw new IllegalStateException(
          "Fondos insuficientes: saldo=" + this.availableBudget + ", requerido=" + amount);
    }
    this.availableBudget = this.availableBudget.subtract(amount);

    // Recalcular burn rate
    if (this.totalBudget.compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal consumed = this.totalBudget.subtract(this.availableBudget);
      this.burnRate =
          consumed
              .multiply(new BigDecimal("100"))
              .divide(this.totalBudget, 2, java.math.RoundingMode.HALF_UP);
    }

    if (this.availableBudget.compareTo(BigDecimal.ZERO) == 0) {
      registerEvent(new FundingSourceProjectExhaustedEvent(this.sourceId, this.projectCode));
    }
  }

  /**
   * Ajusta la distribución del costo laboral. Invariante: la suma de todos los porcentajes DEBE ser
   * exactamente 100%. Evento: COST_CENTER_SPLIT_ADJUSTED (Sync).
   */
  public void adjustCostSplit(List<LaborCostSplit> newSplits) {
    validateSplitSumIs100(newSplits);
    this.costSplits = new ArrayList<>(newSplits);
    registerEvent(new CostCenterSplitAdjustedEvent(this.sourceId, newSplits));
  }

  private void validateSplitSumIs100(List<LaborCostSplit> splits) {
    if (splits == null || splits.isEmpty()) {
      throw new IllegalArgumentException("LaborCostSplit no puede estar vacío");
    }
    BigDecimal sum =
        splits.stream().map(LaborCostSplit::percentage).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (sum.compareTo(new BigDecimal("100.00")) != 0) {
      throw new IllegalStateException(
          "Invariante Consistencia del 100%: la suma de porcentajes es "
              + sum
              + ", debe ser 100.00");
    }
  }

  // --- Getters (sin anotaciones) ---

  public UUID getSourceId() {
    return sourceId;
  }

  public String getProjectCode() {
    return projectCode;
  }

  public BigDecimal getTotalBudget() {
    return totalBudget;
  }

  public BigDecimal getAvailableBudget() {
    return availableBudget;
  }

  public BigDecimal getBurnRate() {
    return burnRate;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public List<LaborCostSplit> getCostSplits() {
    return Collections.unmodifiableList(costSplits);
  }
}
