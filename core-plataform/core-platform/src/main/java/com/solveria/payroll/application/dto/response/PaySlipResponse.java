package com.solveria.payroll.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PaySlipResponse(
    // -- CABECERA --
    String companyName,
    String period, // "Mayo 2026"
    UUID employeeId,
    String fullName,
    String ci,
    String position,
    String department,
    LocalDate hireDate,
    int workedDays,

    // -- INGRESOS --
    IncomeSection incomes,

    // -- DESCUENTOS --
    DeductionSection deductions,

    // -- LIQUIDACIÓN --
    SettlementSection settlement) {
  public record IncomeSection(
      BigDecimal basicSalary,
      BigDecimal seniorityBonus,
      List<VariableIncome> variableIncomes, // Horas extra, comisiones, recargos
      BigDecimal totalEarned) {}

  public record VariableIncome(String concept, BigDecimal amount) {}

  public record DeductionSection(
      GestoraDetail
          gestora, // Desglose: vejez 10%, riesgoComun 1.71%, solidario 0.5%, comisionAfp 0.5%
      BigDecimal rcIvaRetained,
      List<OperationalDeduction> operationalDeductions, // Faltas, préstamos, anticipos
      BigDecimal totalDeductions) {}

  public record GestoraDetail(
      BigDecimal aporteVejez, // 10%
      BigDecimal riesgoComun, // 1.71%
      BigDecimal aporteSolidario, // 0.5%
      BigDecimal comisionAfp, // 0.5%
      BigDecimal totalGestora // 12.71%
      ) {}

  public record OperationalDeduction(String concept, BigDecimal amount) {}

  public record SettlementSection(
      BigDecimal liquidoPagable, // Total Ganado - Total Descuentos
      BigDecimal fiscalCredit // Saldo informativo Form 110
      ) {}
}
