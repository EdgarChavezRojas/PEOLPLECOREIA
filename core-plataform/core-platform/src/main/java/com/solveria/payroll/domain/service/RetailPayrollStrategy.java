package com.solveria.payroll.domain.service;

import com.solveria.payroll.domain.model.entity.PayrollLine;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class RetailPayrollStrategy implements PayrollCalculationStrategy {
    private static final BigDecimal GESTORA_RATE = new BigDecimal("0.1271");
    private static final BigDecimal RC_IVA_RATE = new BigDecimal("0.13");
    private static final BigDecimal SMN = new BigDecimal("3300.00");
    
    @Override
    public void calculate(PayrollLine line, int seniorityYears, BigDecimal fiscalCredit) {
        getBonus(line, seniorityYears, fiscalCredit, SMN, GESTORA_RATE, RC_IVA_RATE);
    }

    static void getBonus(PayrollLine line, int seniorityYears, BigDecimal fiscalCredit, BigDecimal smn, BigDecimal gestoraRate, BigDecimal rcIvaRate) {
        BigDecimal seniorityBonus = BigDecimal.ZERO;
        if (seniorityYears >= 2) {
            seniorityBonus = smn.multiply(new BigDecimal("3"));
        }

        getSalary(line, fiscalCredit, gestoraRate, rcIvaRate, seniorityBonus);
    }

    static void getSalary(PayrollLine line, BigDecimal fiscalCredit, BigDecimal gestoraRate, BigDecimal rcIvaRate, BigDecimal seniorityBonus) {
        BigDecimal totalEarned = line.getBasicSalary().add(seniorityBonus);
        line.setTotalEarned(totalEarned);

        BigDecimal gestora = totalEarned.multiply(gestoraRate).setScale(2, RoundingMode.HALF_UP);
        line.setGestoraRetained(gestora);

        BigDecimal baseRcIva = totalEarned.subtract(gestora).multiply(rcIvaRate);
        BigDecimal rcIva = baseRcIva.subtract(fiscalCredit != null ? fiscalCredit : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        if (rcIva.compareTo(BigDecimal.ZERO) < 0) rcIva = BigDecimal.ZERO;

        line.setRcIvaRetained(rcIva);

        line.recalculateNet();
    }
}
