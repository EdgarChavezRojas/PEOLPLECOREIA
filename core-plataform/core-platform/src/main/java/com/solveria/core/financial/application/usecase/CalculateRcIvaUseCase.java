package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.domain.service.BolivianTaxCalculationService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case: Calcular RC-IVA standalone. Fórmula: [SN - (2 * SMN)] * 13% - (1 * SMN) * 13% -
 * Form110.verifiedCredit
 *
 * <p>Este UC se usa cuando se necesita el cálculo sin la importación del Form 110. Para el flujo
 * completo con Form 110, ver ImportTaxForm110UseCase.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculateRcIvaUseCase {

  /**
   * Calcula el RC-IVA para un sueldo neto dado.
   *
   * @param sueldoNeto Sueldo neto del trabajador
   * @param verifiedCredit Crédito verificado del Form 110 (puede ser null/ZERO)
   * @param isInfocalApplicable Si el tenant es Retail/Corp en Santa Cruz
   * @param totalGanado Total ganado (para INFOCAL)
   * @return Resultado del cálculo con desglose
   */
  public RcIvaResult calculate(
      BigDecimal sueldoNeto,
      BigDecimal verifiedCredit,
      boolean isInfocalApplicable,
      BigDecimal totalGanado) {
    log.info(
        "event=CALCULATE_RC_IVA sueldoNeto={} verifiedCredit={} infocal={}",
        sueldoNeto,
        verifiedCredit,
        isInfocalApplicable);

    BigDecimal rcIva = BolivianTaxCalculationService.calculateRcIva(sueldoNeto, verifiedCredit);
    BigDecimal gestora = BolivianTaxCalculationService.calculateGestoraDeduction(totalGanado);
    BigDecimal infocal =
        isInfocalApplicable
            ? BolivianTaxCalculationService.calculateInfocalScz(totalGanado)
            : BigDecimal.ZERO;

    log.info("event=RC_IVA_RESULT rcIva={} gestora={} infocal={}", rcIva, gestora, infocal);

    return new RcIvaResult(rcIva, gestora, infocal);
  }

  /** Resultado del cálculo RC-IVA con desglose de deducciones. */
  public record RcIvaResult(
      BigDecimal rcIvaAmount, BigDecimal gestoraDeduction, BigDecimal infocalScz) {
    public BigDecimal totalDeductions() {
      return rcIvaAmount.add(gestoraDeduction).add(infocalScz);
    }
  }
}
