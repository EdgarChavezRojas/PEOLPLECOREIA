package com.solveria.payroll.application.web;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.payroll.application.dto.request.GeneratePayrollRequest;
import com.solveria.payroll.application.dto.request.ReviewPayrollRequest;
import com.solveria.payroll.application.dto.response.PaySlipResponse;
import com.solveria.payroll.application.dto.response.PayrollRunDetailResponse;
import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import com.solveria.payroll.application.port.inbound.ApprovePayrollUseCase;
import com.solveria.payroll.application.port.inbound.ClosePayrollUseCase;
import com.solveria.payroll.application.port.inbound.GeneratePayrollUseCase;
import com.solveria.payroll.application.port.inbound.GetEmployeePaySlipUseCase;
import com.solveria.payroll.application.port.inbound.GetPayrollRunByPeriodUseCase;
import com.solveria.payroll.application.port.inbound.ListAllPayrollRunsUseCase;
import com.solveria.payroll.application.port.inbound.ReviewPayrollUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll-runs")
@Tag(
    name = "Payroll Runs",
    description = "Endpoints para la gestión del ciclo de vida de la planilla")
public class PayrollRunController {

  private final GeneratePayrollUseCase generatePayrollUseCase;
  private final ApprovePayrollUseCase approvePayrollUseCase;
  private final ReviewPayrollUseCase reviewPayrollUseCase;
  private final ClosePayrollUseCase closePayrollUseCase;
  private final GetPayrollRunByPeriodUseCase getPayrollRunByPeriodUseCase;
  private final GetEmployeePaySlipUseCase getEmployeePaySlipUseCase;
  private final ListAllPayrollRunsUseCase listAllPayrollRunsUseCase;

  public PayrollRunController(
      GeneratePayrollUseCase generatePayrollUseCase,
      ApprovePayrollUseCase approvePayrollUseCase,
      ReviewPayrollUseCase reviewPayrollUseCase,
      ClosePayrollUseCase closePayrollUseCase,
      GetPayrollRunByPeriodUseCase getPayrollRunByPeriodUseCase,
      GetEmployeePaySlipUseCase getEmployeePaySlipUseCase,
      ListAllPayrollRunsUseCase listAllPayrollRunsUseCase) {
    this.generatePayrollUseCase = generatePayrollUseCase;
    this.approvePayrollUseCase = approvePayrollUseCase;
    this.reviewPayrollUseCase = reviewPayrollUseCase;
    this.closePayrollUseCase = closePayrollUseCase;
    this.getPayrollRunByPeriodUseCase = getPayrollRunByPeriodUseCase;
    this.getEmployeePaySlipUseCase = getEmployeePaySlipUseCase;
    this.listAllPayrollRunsUseCase = listAllPayrollRunsUseCase;
  }

  @GetMapping
  @Operation(
      summary = "Listar todas las planillas",
      description =
          "Retorna todas las planillas del tenant autenticado ordenadas por fecha de creación descendente.")
  public ResponseEntity<List<PayrollRunResponse>> listAll() {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    List<PayrollRunResponse> response = listAllPayrollRunsUseCase.execute(tenantId);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @Operation(
      summary = "Crear/Generar borrador de planilla",
      description = "Genera un nuevo borrador de planilla para un periodo determinado.")
  public ResponseEntity<PayrollRunResponse> generateDraft(
      @RequestBody GeneratePayrollRequest request) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    PayrollRunResponse response = generatePayrollUseCase.execute(request, tenantId);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PostMapping("/{approvalId}/approve")
  @Operation(summary = "Aprobar planilla", description = "Aprueba una planilla en estado borrador.")
  public ResponseEntity<Void> approvePayroll(@PathVariable UUID approvalId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    approvePayrollUseCase.execute(approvalId, tenantId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{runId}/review")
  @Operation(summary = "Revisar planilla", description = "Marca una planilla en revisión.")
  public ResponseEntity<Void> reviewPayroll(
      @PathVariable UUID runId, @RequestBody ReviewPayrollRequest request) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    reviewPayrollUseCase.execute(runId, request, tenantId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{runId}/close")
  @Operation(
      summary = "Cerrar planilla",
      description = "Cierra definitivamente una planilla aprobada.")
  public ResponseEntity<Void> closePayroll(
      @PathVariable UUID runId) { // 👈 Corregido con el nombre explícito
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    closePayrollUseCase.execute(runId, tenantId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/period/{periodId}")
  @Operation(
      summary = "Obtener planillas del período",
      description = "Recupera los detalles de las planillas por período y tenant.")
  public ResponseEntity<List<PayrollRunDetailResponse>> getPayrollRunByPeriod(
      @PathVariable UUID periodId) { // 👈 Corregido con el nombre explícito
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    List<PayrollRunDetailResponse> response =
        getPayrollRunByPeriodUseCase.execute(periodId, tenantId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{runId}/employees/{employeeId}/slip")
  @Operation(
      summary = "Obtener boleta de pago de un empleado",
      description = "Genera la boleta detallada para un empleado dentro de una planilla.")
  public ResponseEntity<PaySlipResponse> getEmployeePaySlip(
      @PathVariable UUID runId, // 👈 Corregido con el nombre explícito
      @PathVariable UUID employeeId // 👈 Corregido con el nombre explícito
      ) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    PaySlipResponse response = getEmployeePaySlipUseCase.execute(runId, employeeId, tenantId);
    return ResponseEntity.ok(response);
  }
}
