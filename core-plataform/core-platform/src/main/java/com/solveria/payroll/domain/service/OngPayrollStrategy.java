package com.solveria.payroll.domain.service;

import com.solveria.payroll.domain.model.entity.PayrollLine;
import java.math.BigDecimal;

public class OngPayrollStrategy implements PayrollCalculationStrategy {
  private static final BigDecimal GESTORA_RATE = new BigDecimal("0.1271");
  private static final BigDecimal RC_IVA_RATE = new BigDecimal("0.13");
  private static final BigDecimal SMN = new BigDecimal("3300.00");

  @Override
  public void calculate(PayrollLine line, int seniorityYears, BigDecimal fiscalCredit) {
    BigDecimal seniorityBonus = BigDecimal.ZERO;
    if (seniorityYears >= 2) {
      seniorityBonus = SMN;
    }

    RetailPayrollStrategy.getSalary(line, fiscalCredit, GESTORA_RATE, RC_IVA_RATE, seniorityBonus);
  }
}
