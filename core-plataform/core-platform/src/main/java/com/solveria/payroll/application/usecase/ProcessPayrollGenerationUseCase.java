package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.dto.request.EligibleEmployee;
import com.solveria.payroll.application.dto.request.GeneratePayrollRequest;
import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import com.solveria.payroll.application.port.inbound.GeneratePayrollUseCase;
import com.solveria.payroll.application.port.outbound.DeductionRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.EligibleEmployeePort;
import com.solveria.payroll.application.port.outbound.IncomeRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.application.port.outbound.TenantProfilePort;
import com.solveria.payroll.domain.model.ar.DeductionRecord;
import com.solveria.payroll.domain.model.ar.IncomeRecord;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import com.solveria.payroll.domain.model.entity.PayrollLine;
import com.solveria.payroll.domain.model.vo.PayrollRunType;
import com.solveria.payroll.domain.model.vo.PayrollStatus;
import com.solveria.payroll.domain.model.vo.TenantProfile;
import com.solveria.payroll.domain.service.CorpPayrollStrategy;
import com.solveria.payroll.domain.service.OngPayrollStrategy;
import com.solveria.payroll.domain.service.PayrollCalculationStrategy;
import com.solveria.payroll.domain.service.RetailPayrollStrategy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessPayrollGenerationUseCase implements GeneratePayrollUseCase {

  private final PayrollRunRepositoryPort payrollRunRepositoryPort;
  private final EligibleEmployeePort eligibleEmployeePort;
  private final TenantProfilePort tenantProfilePort;
  private final IncomeRecordRepositoryPort incomeRecordRepositoryPort;
  private final DeductionRecordRepositoryPort deductionRecordRepositoryPort;

  public ProcessPayrollGenerationUseCase(
      PayrollRunRepositoryPort payrollRunRepositoryPort,
      EligibleEmployeePort eligibleEmployeePort,
      TenantProfilePort tenantProfilePort,
      IncomeRecordRepositoryPort incomeRecordRepositoryPort,
      DeductionRecordRepositoryPort deductionRecordRepositoryPort) {
    this.payrollRunRepositoryPort = payrollRunRepositoryPort;
    this.eligibleEmployeePort = eligibleEmployeePort;
    this.tenantProfilePort = tenantProfilePort;
    this.incomeRecordRepositoryPort = incomeRecordRepositoryPort;
    this.deductionRecordRepositoryPort = deductionRecordRepositoryPort;
  }

  @Override
  @Transactional
  public PayrollRunResponse execute(GeneratePayrollRequest request, UUID tenantId) {
    // 1. Resolver el perfil del tenant para seleccionar la estrategia
    TenantProfile tenantProfile = tenantProfilePort.resolve(tenantId);
    PayrollCalculationStrategy strategy;
    switch (tenantProfile) {
      case ONG -> strategy = new OngPayrollStrategy();
      case RETAIL -> strategy = new RetailPayrollStrategy();
      default -> strategy = new CorpPayrollStrategy();
    }

    // 2. Crear la cabecera de la planilla (PayrollRun)
    PayrollRun run =
        new PayrollRun(
            UUID.randomUUID(),
            request.periodId(),
            null,
            PayrollRunType.valueOf(request.runType()),
            PayrollStatus.BORRADOR,
            tenantId,
            new ArrayList<>());

    // 3. Cargar los empleados elegibles (con onboarding completed)
    List<EligibleEmployee> eligibleEmployees =
        eligibleEmployeePort.findEligibleByTenantId(tenantId);

    // 4. Procesar y calcular la planilla para cada empleado
    for (EligibleEmployee emp : eligibleEmployees) {
      // 4.1. Crear la línea inicial con salario básico del contrato
      PayrollLine line =
          new PayrollLine(
              UUID.randomUUID(),
              run.getId(),
              emp.employeeId(),
              emp.basicSalary(),
              BigDecimal.ZERO,
              BigDecimal.ZERO,
              BigDecimal.ZERO,
              BigDecimal.ZERO,
              BigDecimal.ZERO,
              tenantId,
              BigDecimal.ZERO,
              BigDecimal.ZERO,
              emp.fiscalCredit());

      // 4.2. Consolidar egresos/descuentos del período para el empleado (faltas, atrasos,
      // préstamos, etc.)
      List<DeductionRecord> deductions =
          deductionRecordRepositoryPort.findByEmployeeAndPeriod(
              emp.employeeId(), request.periodId(), tenantId);
      BigDecimal otherDeductions =
          deductions.stream()
              .map(d -> d.getAmount().value())
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      line.setOtherDeductions(otherDeductions);

      // 4.3. Consolidar ingresos variables (horas extra, comisiones, etc.)
      List<IncomeRecord> incomes =
          incomeRecordRepositoryPort.findByEmployeeAndPeriod(
              emp.employeeId(), request.periodId(), tenantId);
      BigDecimal variableIncomes =
          incomes.stream().map(i -> i.getAmount().value()).reduce(BigDecimal.ZERO, BigDecimal::add);

      // 4.4. Setear temporalmente basicSalary para incluir ingresos variables
      // Así la estrategia calcula Gestora, INFOCAL y RC-IVA sobre el Total Ganado real
      BigDecimal contractSalary = emp.basicSalary();
      line.setBasicSalary(contractSalary.add(variableIncomes));

      // 4.5. Aplicar estrategia de cálculo
      strategy.calculate(line, emp.seniorityYears(), emp.fiscalCredit());

      // 4.6. Restaurar el salario básico contractual en la línea
      line.setBasicSalary(contractSalary);

      // 4.7. Agregar la línea procesada al PayrollRun
      run.addLine(line);
    }

    // 5. Generar borrador (valida invariantes y líquida pagable no negativo)
    run.generateDraft();

    // 6. Persistir planilla completa
    payrollRunRepositoryPort.save(run);

    // 7. Calcular totales reales para la respuesta
    BigDecimal totalGrossAmount = BigDecimal.ZERO;
    BigDecimal totalNetAmount = BigDecimal.ZERO;
    for (PayrollLine line : run.getLines()) {
      totalGrossAmount = totalGrossAmount.add(line.getTotalEarned());
      totalNetAmount = totalNetAmount.add(line.getNetPayable());
    }

    return new PayrollRunResponse(
        run.getId(),
        run.getPeriodRef(),
        run.getTenantId(),
        run.getRunType().name(),
        run.getStatus().name(),
        totalGrossAmount,
        totalNetAmount,
        LocalDateTime.now(),
        LocalDateTime.now());
  }
}
