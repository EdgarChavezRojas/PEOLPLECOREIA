package com.solveria.core.financial.application.command;

import com.solveria.core.financial.domain.model.vo.TerminationType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Command: Procesar liquidación (Finiquito). W9: Offboarding y Liquidación. */
public record ProcessLiquidationCommand(
    UUID relationshipId,
    UUID personId,
    TerminationType terminationType,
    LocalDate terminationDate,
    LocalDate hireDate,
    List<BigDecimal> lastThreeMonthsSalaries,
    List<BigDecimal> lastThreeMonthsBase,
    List<BigDecimal> lastThreeMonthsOthers,
    int pendingVacationDays,
    UUID tenantId,
    String approverUserId) {
  public ProcessLiquidationCommand {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es obligatorio");
    }
    if (personId == null) {
      throw new IllegalArgumentException("personId es obligatorio");
    }
    if (terminationType == null) {
      throw new IllegalArgumentException("terminationType es obligatorio");
    }
    if (terminationDate == null) {
      throw new IllegalArgumentException("terminationDate es obligatorio");
    }
    if (hireDate == null) {
      throw new IllegalArgumentException("hireDate es obligatorio");
    }
    if (approverUserId == null || approverUserId.isBlank()) {
      throw new IllegalArgumentException("approverUserId es obligatorio para SoD");
    }
  }
}

