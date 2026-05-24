package com.solveria.core.legal.application.web;

import com.solveria.core.legal.application.dto.*;
import com.solveria.core.legal.application.dto.webRequest.ApproveContractAddendumWebDto;
import com.solveria.core.legal.application.dto.webRequest.DraftContractWebDto;
import com.solveria.core.legal.application.dto.webRequest.ProposeContractAddendumWebDto;
import com.solveria.core.legal.application.dto.webRequest.TerminateContractWebDto;
import com.solveria.core.legal.application.usecase.ApproveContractAddendumUseCase;
import com.solveria.core.legal.application.usecase.ApproveContractUseCase;
import com.solveria.core.legal.application.usecase.DraftContractUseCase;
import com.solveria.core.legal.application.usecase.ProposeContractAddendumUseCase;
import com.solveria.core.legal.application.usecase.TerminateContractUseCase;
import com.solveria.core.security.context.SecurityTenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/legal/contracts")
@Tag(
    name = "Contract Lifecycle",
    description =
        "API para la gestión del ciclo de vida de los contratos laborales, adendas y desvinculaciones")
public class ContractLifecycleController {

  private final DraftContractUseCase draftContractUseCase;
  private final ApproveContractUseCase approveContractUseCase;
  private final ProposeContractAddendumUseCase proposeContractAddendumUseCase;
  private final ApproveContractAddendumUseCase approveContractAddendumUseCase;
  private final TerminateContractUseCase terminateContractUseCase;

  public ContractLifecycleController(
      DraftContractUseCase draftContractUseCase,
      ApproveContractUseCase approveContractUseCase,
      ProposeContractAddendumUseCase proposeContractAddendumUseCase,
      ApproveContractAddendumUseCase approveContractAddendumUseCase,
      TerminateContractUseCase terminateContractUseCase) {
    this.draftContractUseCase = draftContractUseCase;
    this.approveContractUseCase = approveContractUseCase;
    this.proposeContractAddendumUseCase = proposeContractAddendumUseCase;
    this.approveContractAddendumUseCase = approveContractAddendumUseCase;
    this.terminateContractUseCase = terminateContractUseCase;
  }

  // -------------------------------------------------------------------
  // 1. Redactar Borrador de Contrato
  // -------------------------------------------------------------------
  @PostMapping("/draft")
  @Operation(
      summary = "Redactar un contrato (Borrador)",
      description = "Crea un contrato en estado DRAFT y reserva la plaza (Position) presupuestada.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Borrador creado exitosamente"),
    @ApiResponse(
        responseCode = "400",
        description = "Bad Request: Datos incompletos o salario por debajo del SMN (Bs 3.300)",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden: Error de TenantMismatch",
        content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict: La posición ya está ocupada o no hay presupuesto",
        content = @Content)
  })
  public ResponseEntity<ContractResponse> draftContract(
      @Valid @RequestBody DraftContractWebDto request) {
    UUID tenantUuid = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    ContractResponse response = draftContractUseCase.execute(request.toCommand(tenantUuid));

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // -------------------------------------------------------------------
  // 2. Aprobar Contrato
  // -------------------------------------------------------------------
  @PostMapping("/approve")
  @Operation(
      summary = "Aprobar contrato",
      description =
          "Aprueba un contrato en borrador. Aplica Segregación de Funciones (SoD): El aprobador no puede ser el creador del borrador.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Contrato aprobado y activado exitosamente"),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden: Violación de Segregación de Funciones (SoD) o falta de permisos",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Not Found: Contrato no encontrado",
        content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict: El contrato ya fue aprobado previamente o no está en estado DRAFT",
        content = @Content)
  })
  public ResponseEntity<Void> approveContract(@RequestBody ApproveContractRequest request) {

    approveContractUseCase.execute(request);
    return ResponseEntity.ok().build();
  }

  // -------------------------------------------------------------------
  // 3. Proponer Adenda Contractual
  // -------------------------------------------------------------------

  @PostMapping("/{contractId}/addendums/propose")
  @Operation(
      summary = "Proponer una adenda",
      description =
          "Propone un cambio de salario, cargo o jornada. Si el incremento es mayor al 15%, saltará a revisión de Finanzas.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Adenda propuesta creada (Pending)"),
    @ApiResponse(
        responseCode = "400",
        description =
            "Bad Request: Salario propuesto menor al SMN o alerta de retención RC-IVA (P13)",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Not Found: Contrato base no encontrado",
        content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict: Hay otra adenda pendiente de aprobación o fechas superpuestas",
        content = @Content)
  })
  public ResponseEntity<ContractAddendumResponse> proposeAddendum(
      @Valid @RequestBody ProposeContractAddendumWebDto request) {
    UUID tenantUuid = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    ContractAddendumResponse response =
        proposeContractAddendumUseCase.execute(request.toCommand(tenantUuid));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // -------------------------------------------------------------------
  // 4. Aprobar Adenda Contractual
  // -------------------------------------------------------------------
  @PostMapping("/{contractId}/addendums/{addendumId}/approve")
  @Operation(
      summary = "Aprobar adenda propuesta",
      description =
          "Aprueba y aplica la adenda al contrato, generando una nueva línea de tiempo (Effective Dating).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Adenda aprobada exitosamente"),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden: Violación de Segregación de Funciones (SoD)",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Not Found: Contrato o Adenda no encontrados",
        content = @Content)
  })
  public ResponseEntity<Void> approveAddendum(@RequestBody ApproveContractAddendumWebDto request) {

    UUID tenantUuid = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    approveContractAddendumUseCase.execute(request.toCommand(tenantUuid));
    return ResponseEntity.ok().build();
  }

  // -------------------------------------------------------------------
  // 5. Finalizar Contrato (Offboarding)
  // -------------------------------------------------------------------
  @PostMapping("/{contractId}/terminate")
  @Operation(
      summary = "Finalizar contrato",
      description =
          "Da de baja un contrato activo e inicia el cálculo para el Finiquito. Notifica a Payroll.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Contrato finalizado exitosamente"),
    @ApiResponse(
        responseCode = "400",
        description =
            "Bad Request: Fecha de terminación inválida (ej. previa al inicio del contrato)",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Not Found: Contrato no encontrado",
        content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict: El contrato no está en estado ACTIVE o ya fue finalizado",
        content = @Content)
  })
  public ResponseEntity<Void> terminateContract(@RequestBody TerminateContractWebDto request) {
    UUID tenantUuid = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    terminateContractUseCase.execute(request.toCommand(tenantUuid));
    return ResponseEntity.ok().build();
  }
}
