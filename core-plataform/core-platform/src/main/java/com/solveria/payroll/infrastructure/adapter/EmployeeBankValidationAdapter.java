package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.BankAccountVerificationPort;
import com.solveria.payroll.application.port.outbound.EmployeeBankValidationPort;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.ar.PayrollRun;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EmployeeBankValidationAdapter implements EmployeeBankValidationPort {

  private final PayrollRunRepositoryPort payrollRunRepositoryPort;
  private final BankAccountVerificationPort bankAccountVerificationPort;

  public EmployeeBankValidationAdapter(
      PayrollRunRepositoryPort payrollRunRepositoryPort,
      BankAccountVerificationPort bankAccountVerificationPort) {
    this.payrollRunRepositoryPort = payrollRunRepositoryPort;
    this.bankAccountVerificationPort = bankAccountVerificationPort;
  }

  @Override
  public boolean allEmployeesHaveBankAccount(UUID runId, UUID tenantId) {
    // 1. Cargar la corrida de planilla con sus líneas ávidas procesadas
    PayrollRun run =
        payrollRunRepositoryPort
            .findByIdWithLines(runId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No se encontró la corrida de planilla especificada."));

    // 2. Validar que cada empleado de la nómina cumpla con la invariante de cuenta bancaria activa
    // y sincronizada
    // No se permite generación parcial de archivos bancarios (Invariante de Control de Dispersión)
    return run.getLines().stream()
        .allMatch(
            line ->
                bankAccountVerificationPort.isBankAccountValidated(line.getEmployeeId(), tenantId));
  }
}
