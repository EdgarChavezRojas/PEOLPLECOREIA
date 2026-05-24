package com.solveria.payroll.domain.service;

import com.solveria.core.financial.domain.service.BolivianTaxCalculationService;
import com.solveria.payroll.domain.model.entity.PayrollLine;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class RetailPayrollStrategy implements PayrollCalculationStrategy {

  @Override
  public void calculate(PayrollLine line, int seniorityYears, BigDecimal fiscalCredit) {
    BigDecimal smn = BolivianTaxCalculationService.SMN;
    BigDecimal base = smn.multiply(new BigDecimal("3"));

    BigDecimal percentage = BigDecimal.ZERO;
    if (seniorityYears >= 25) {
      percentage = new BigDecimal("0.50");
    } else if (seniorityYears >= 20) {
      percentage = new BigDecimal("0.42");
    } else if (seniorityYears >= 15) {
      percentage = new BigDecimal("0.34");
    } else if (seniorityYears >= 11) {
      percentage = new BigDecimal("0.26");
    } else if (seniorityYears >= 8) {
      percentage = new BigDecimal("0.18");
    } else if (seniorityYears >= 5) {
      percentage = new BigDecimal("0.11");
    } else if (seniorityYears >= 2) {
      percentage = new BigDecimal("0.05");
    }

    BigDecimal seniorityBonus = base.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
    line.setSeniorityBonus(seniorityBonus);

    BigDecimal totalEarned = line.getBasicSalary().add(seniorityBonus);
    line.setTotalEarned(totalEarned);

    BigDecimal gestora = BolivianTaxCalculationService.calculateGestoraDeduction(totalEarned);
    line.setGestoraRetained(gestora);

    BigDecimal infocal = BolivianTaxCalculationService.calculateInfocalScz(totalEarned);
    line.setInfocalRetained(infocal);

    BigDecimal sueldoNetoProvisional = totalEarned.subtract(gestora).subtract(infocal);
    BigDecimal rcIva =
        BolivianTaxCalculationService.calculateRcIva(sueldoNetoProvisional, fiscalCredit);
    line.setRcIvaRetained(rcIva);

    line.setFiscalCredit(fiscalCredit != null ? fiscalCredit : BigDecimal.ZERO);
    line.recalculateNet();
  }
}
