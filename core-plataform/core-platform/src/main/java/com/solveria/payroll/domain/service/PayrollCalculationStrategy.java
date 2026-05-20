package com.solveria.payroll.domain.service;

import com.solveria.payroll.domain.model.entity.PayrollLine;
import java.math.BigDecimal;

public interface PayrollCalculationStrategy {
  void calculate(PayrollLine line, int seniorityYears, BigDecimal fiscalCredit);
}
