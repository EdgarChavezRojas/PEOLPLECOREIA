package com.solveria.core.financial.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/** Command: Validar suficiencia de fondos en un FundingSource. W1: ONG Onboarding Budget Check. */
public record ValidateFundingSourceCommand(
    UUID sourceId, BigDecimal requiredAmount, String approverUserId) {
  public ValidateFundingSourceCommand {
    if (sourceId == null) {
      throw new IllegalArgumentException("sourceId es obligatorio");
    }
    if (requiredAmount == null || requiredAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("requiredAmount debe ser positivo");
    }
    if (approverUserId == null || approverUserId.isBlank()) {
      throw new IllegalArgumentException("approverUserId es obligatorio para SoD");
    }
  }
}
