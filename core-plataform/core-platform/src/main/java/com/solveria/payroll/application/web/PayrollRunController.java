package com.solveria.payroll.application.web;

import com.solveria.payroll.application.dto.request.ApprovePayrollRequest;
import com.solveria.payroll.application.dto.request.GeneratePayrollRequest;
import com.solveria.payroll.application.dto.response.PayrollRunResponse;
import com.solveria.payroll.application.port.inbound.ApprovePayrollUseCase;
import com.solveria.payroll.application.port.inbound.ClosePayrollUseCase;
import com.solveria.payroll.application.port.inbound.GeneratePayrollUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
  private final ClosePayrollUseCase closePayrollUseCase;

  public PayrollRunController(
      GeneratePayrollUseCase generatePayrollUseCase,
      ApprovePayrollUseCase approvePayrollUseCase,
      ClosePayrollUseCase closePayrollUseCase) {
    this.generatePayrollUseCase = generatePayrollUseCase;
    this.approvePayrollUseCase = approvePayrollUseCase;
    this.closePayrollUseCase = closePayrollUseCase;
  }

  @PostMapping
  @Operation(
      summary = "Crear/Generar borrador de planilla",
      description = "Genera un nuevo borrador de planilla para un periodo determinado.")
  public ResponseEntity<PayrollRunResponse> generateDraft(
      @RequestBody GeneratePayrollRequest request, @RequestHeader("X-Tenant-ID") UUID tenantId) {
    PayrollRunResponse response = generatePayrollUseCase.execute(request, tenantId);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PostMapping("/{runId}/approve")
  @Operation(summary = "Aprobar planilla", description = "Aprueba una planilla en estado borrador.")
  public ResponseEntity<Void> approvePayroll(
      @PathVariable UUID runId,
      @RequestBody ApprovePayrollRequest request,
      @RequestHeader("X-Tenant-ID") UUID tenantId) {
    approvePayrollUseCase.execute(runId, request, tenantId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{runId}/close")
  @Operation(
      summary = "Cerrar planilla",
      description = "Cierra definitivamente una planilla aprobada.")
  public ResponseEntity<Void> closePayroll(
      @PathVariable UUID runId, @RequestHeader("X-Tenant-ID") UUID tenantId) {
    closePayrollUseCase.execute(runId, tenantId);
    return ResponseEntity.ok().build();
  }
}
