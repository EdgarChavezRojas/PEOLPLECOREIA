package com.solveria.core.financial.application.port;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Primary Port: Tax Compliance (RC-IVA, Form 110). Expone operaciones de cálculo tributario y
 * gestión de formularios fiscales.
 */
public interface TaxCompliancePort {

  /**
   * Importa un Formulario 110 del SIAT. Calcula automáticamente verifiedCredit = totalDeclared *
   * 13%.
   */
  UUID importTaxForm110(
      UUID personId,
      BigDecimal totalDeclared,
      UUID docId,
      YearMonth period,
      String tenantId,
      String createdBy);

  /**
   * Calcula el RC-IVA para un trabajador. Fórmula: [SN - (2*SMN)] * 13% - (1*SMN) * 13% -
   * Form110.verifiedCredit
   */
  BigDecimal calculateRcIva(UUID personId, BigDecimal sueldoNeto, YearMonth period);
}
