package com.solveria.core.financial.application.command;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/** Command: Importar Formulario 110 del SIAT. */
public record ImportTaxForm110Command(
    UUID personId,
    BigDecimal totalDeclared,
    UUID docId,
    YearMonth period,
    UUID tenantId,
    String createdBy) {
  public ImportTaxForm110Command {
    if (personId == null) {
      throw new IllegalArgumentException("personId es obligatorio");
    }
    if (totalDeclared == null || totalDeclared.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("totalDeclared no puede ser negativo");
    }
    if (period == null) {
      throw new IllegalArgumentException("period es obligatorio");
    }
  }
}
