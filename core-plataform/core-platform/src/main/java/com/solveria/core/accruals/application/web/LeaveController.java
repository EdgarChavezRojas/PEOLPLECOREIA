package com.solveria.core.accruals.application.web;

import com.solveria.core.accruals.application.command.ApproveLeaveCommand;
import com.solveria.core.accruals.application.command.RejectLeaveCommand;
import com.solveria.core.accruals.application.command.RequestLeaveCommand;
import com.solveria.core.accruals.application.dto.RequestLeaveRequest;
import com.solveria.core.accruals.application.dto.ReviewLeaveRequest;
import com.solveria.core.accruals.application.usecase.ApproveLeaveUseCase;
import com.solveria.core.accruals.application.usecase.ListEmployeeLeavesUseCase;
import com.solveria.core.accruals.application.usecase.RejectLeaveUseCase;
import com.solveria.core.accruals.application.usecase.RequestLeaveUseCase;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leaves", description = "API de licencias/ausencias justificadas")
public class LeaveController {

  private static final String DEFAULT_LOCATION = "Santa Cruz, Bolivia";

  private final RequestLeaveUseCase requestLeaveUseCase;
  private final ApproveLeaveUseCase approveLeaveUseCase;
  private final RejectLeaveUseCase rejectLeaveUseCase;
  private final ListEmployeeLeavesUseCase listEmployeeLeavesUseCase;

  @GetMapping
  @Operation(
          summary = "Registrar licencia en el motor de saldos",
          description = "Crea y asienta una transacción de licencia directamente en el saldo del colaborador. " +
                  "Esta operación es administrativa y se utiliza para integraciones directas o " +
                  "ajustes de RRHH. No activa el flujo de aprobación gerencial de Experience BC.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Listado paginado obtenido"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Page<LeaveTransaction>> list(
      @RequestParam UUID personId, Pageable pageable) {
    return ResponseEntity.ok(listEmployeeLeavesUseCase.handle(personId, pageable));
  }

  @PostMapping
  @Operation(summary = "Solicitar licencia", description = "Registra una solicitud de licencia")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Solicitud creada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> requestLeave(@Valid @RequestBody RequestLeaveRequest request) {
    RequestLeaveCommand command =
        new RequestLeaveCommand(
            request.balanceId(), request.startDate(), request.endDate(), DEFAULT_LOCATION);
    requestLeaveUseCase.handle(command);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{transactionId}/approve")
  @Operation(summary = "Asentar aprobación de licencia en la base de datos",
          description = "Ejecuta el descuento matemático y el cierre de estado de una transacción de licencia " +
                  "previamente existente. Esta operación es de bajo nivel y actualiza directamente el AccrualBalance.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Solicitud aprobada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> approveLeave(
      @PathVariable UUID transactionId, @Valid @RequestBody ReviewLeaveRequest request) {
    ApproveLeaveCommand command =
        new ApproveLeaveCommand(request.balanceId(), transactionId, DEFAULT_LOCATION);
    approveLeaveUseCase.handle(command);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{transactionId}/reject")
  @Operation(summary = "Rechazar licencia", description = "Rechaza una solicitud de licencia")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Solicitud rechazada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> rejectLeave(
      @PathVariable UUID transactionId, @Valid @RequestBody ReviewLeaveRequest request) {
    RejectLeaveCommand command =
        new RejectLeaveCommand(request.balanceId(), transactionId, DEFAULT_LOCATION);
    rejectLeaveUseCase.handle(command);
    return ResponseEntity.ok().build();
  }
}
