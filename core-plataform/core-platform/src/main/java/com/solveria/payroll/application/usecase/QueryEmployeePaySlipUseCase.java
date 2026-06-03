package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.dto.request.EligibleEmployee;
import com.solveria.payroll.application.dto.response.PaySlipResponse;
import com.solveria.payroll.application.port.inbound.GetEmployeePaySlipUseCase;
import com.solveria.payroll.application.port.outbound.DeductionRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.EligibleEmployeePort;
import com.solveria.payroll.application.port.outbound.IncomeRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.ar.DeductionRecord;
import com.solveria.payroll.domain.model.ar.IncomeRecord;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import com.solveria.payroll.domain.model.entity.PayrollLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QueryEmployeePaySlipUseCase implements GetEmployeePaySlipUseCase {

  private final PayrollRunRepositoryPort payrollRunRepositoryPort;
  private final EligibleEmployeePort eligibleEmployeePort;
  private final IncomeRecordRepositoryPort incomeRecordRepositoryPort;
  private final DeductionRecordRepositoryPort deductionRecordRepositoryPort;

  public QueryEmployeePaySlipUseCase(
      PayrollRunRepositoryPort payrollRunRepositoryPort,
      EligibleEmployeePort eligibleEmployeePort,
      IncomeRecordRepositoryPort incomeRecordRepositoryPort,
      DeductionRecordRepositoryPort deductionRecordRepositoryPort) {
    this.payrollRunRepositoryPort = payrollRunRepositoryPort;
    this.eligibleEmployeePort = eligibleEmployeePort;
    this.incomeRecordRepositoryPort = incomeRecordRepositoryPort;
    this.deductionRecordRepositoryPort = deductionRecordRepositoryPort;
  }

  @Override
  @Transactional(readOnly = true)
  public PaySlipResponse execute(UUID runId, UUID employeeId, UUID tenantId) {
    // 1. Cargar PayrollRun con sus líneas
    PayrollRun run =
        payrollRunRepositoryPort
            .findByIdWithLines(runId)
            .orElseThrow(
                () -> new IllegalArgumentException("No se encontró la planilla especificada."));

    if (!run.getTenantId().equals(tenantId)) {
      throw new IllegalArgumentException("La planilla no pertenece al tenant especificado.");
    }

    // 2. Buscar la línea específica del empleado
    PayrollLine line =
        run.getLines().stream()
            .filter(l -> l.getEmployeeId().equals(employeeId))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No se encontró una línea para el empleado en esta planilla."));

    // 3. Cargar detalles del empleado
    EligibleEmployee emp = eligibleEmployeePort.findById(employeeId, tenantId);
    if (emp == null) {
      throw new IllegalArgumentException("No se encontraron los datos del empleado especificado.");
    }

    // 4. Cargar ingresos y egresos
    List<IncomeRecord> incomes =
        incomeRecordRepositoryPort.findByEmployeeAndPeriod(
            employeeId, run.getPeriodRef(), tenantId);
    List<DeductionRecord> deductions =
        deductionRecordRepositoryPort.findByEmployeeAndPeriod(
            employeeId, run.getPeriodRef(), tenantId);

    // 5. Desglosar ingresos variables (Blindado contra nulos)
    List<PaySlipResponse.VariableIncome> variableIncomesList = Collections.emptyList();
    if (incomes != null) {
      variableIncomesList =
          incomes.stream()
              .filter(i -> i != null && i.getIncomeType() != null && i.getAmount() != null)
              .map(
                  i ->
                      new PaySlipResponse.VariableIncome(
                          i.getIncomeType().name(), i.getAmount().value()))
              .toList();
    }

    // 6. Desglosar descuentos operativos (Blindado contra nulos)
    List<PaySlipResponse.OperationalDeduction> operationalDeductionsList = Collections.emptyList();
    if (deductions != null) {
      operationalDeductionsList =
          deductions.stream()
              .filter(d -> d != null && d.getDeductionType() != null && d.getAmount() != null)
              .map(
                  d ->
                      new PaySlipResponse.OperationalDeduction(
                          d.getDeductionType().name(), d.getAmount().value()))
              .toList();
    }

    // 7. Desglosar Gestora Pública (10% + 1.71% + 0.5% + 0.5%)
    BigDecimal totalEarned =
        line.getTotalEarned() != null ? line.getTotalEarned() : BigDecimal.ZERO;
    BigDecimal vejez =
        totalEarned.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
    BigDecimal riesgo =
        totalEarned.multiply(new BigDecimal("0.0171")).setScale(2, RoundingMode.HALF_UP);
    BigDecimal solidario =
        totalEarned.multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);

    BigDecimal totalGestora =
        line.getGestoraRetained() != null ? line.getGestoraRetained() : BigDecimal.ZERO;
    BigDecimal comision = totalGestora.subtract(vejez).subtract(riesgo).subtract(solidario);
    if (comision.compareTo(BigDecimal.ZERO) < 0) {
      comision = BigDecimal.ZERO;
    }

    PaySlipResponse.GestoraDetail gestoraDetail =
        new PaySlipResponse.GestoraDetail(vejez, riesgo, solidario, comision, totalGestora);

    // 8. Totales finales (Blindado contra valores nulos de las columnas de la BD)
    BigDecimal rcIva = line.getRcIvaRetained() != null ? line.getRcIvaRetained() : BigDecimal.ZERO;
    BigDecimal infocal =
        line.getInfocalRetained() != null ? line.getInfocalRetained() : BigDecimal.ZERO;
    BigDecimal otherDeductions =
        line.getOtherDeductions() != null ? line.getOtherDeductions() : BigDecimal.ZERO;
    BigDecimal basicSalary =
        line.getBasicSalary() != null ? line.getBasicSalary() : BigDecimal.ZERO;
    BigDecimal seniorityBonus =
        line.getSeniorityBonus() != null ? line.getSeniorityBonus() : BigDecimal.ZERO;
    BigDecimal netPayable = line.getNetPayable() != null ? line.getNetPayable() : BigDecimal.ZERO;
    BigDecimal fiscalCredit =
        line.getFiscalCredit() != null ? line.getFiscalCredit() : BigDecimal.ZERO;

    BigDecimal totalDeductions = totalGestora.add(rcIva).add(infocal).add(otherDeductions);

    PaySlipResponse.IncomeSection incomeSection =
        new PaySlipResponse.IncomeSection(
            basicSalary, seniorityBonus, variableIncomesList, totalEarned);

    PaySlipResponse.DeductionSection deductionSection =
        new PaySlipResponse.DeductionSection(
            gestoraDetail, rcIva, operationalDeductionsList, totalDeductions);

    PaySlipResponse.SettlementSection settlementSection =
        new PaySlipResponse.SettlementSection(netPayable, fiscalCredit);

    // Formatear el periodo de forma segura
    String periodStr = "Periodo";
    if (run.getPeriodRef() != null) {
      String ref = run.getPeriodRef().toString();
      periodStr = "Periodo " + (ref.length() > 8 ? ref.substring(0, 8) : ref);
    }

    return new PaySlipResponse(
        "Solveria RRHH S.A.",
        periodStr,
        employeeId,
        emp.fullName(),
        emp.ci(),
        emp.position(),
        emp.department(),
        emp.hireDate(),
        emp.workedDays() > 0 ? emp.workedDays() : 30,
        incomeSection,
        deductionSection,
        settlementSection);
  }
}
