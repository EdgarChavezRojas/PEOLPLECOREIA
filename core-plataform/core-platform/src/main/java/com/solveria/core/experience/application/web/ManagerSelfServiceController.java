package com.solveria.core.experience.application.web;

import com.solveria.core.experience.application.dto.RejectDataChangeRequest;
import com.solveria.core.experience.application.usecase.ManagerSelfServiceUseCase;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manager-self-service")
@RequiredArgsConstructor
@Tag(name = "MSS - Self Service", description = "Operaciones de aprobación y rechazo del manager")
public class ManagerSelfServiceController {

  private final ManagerSelfServiceUseCase managerSelfServiceUseCase;

  @PostMapping("/actions/{actionId}/approve")
  @Operation(summary = "Aprobar cambio de datos", description = "Aprueba una solicitud ESS")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Solicitud aprobada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> approveAction(@PathVariable UUID actionId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    Long userId = SecurityUserContext.getUserId();
    managerSelfServiceUseCase.approveDataChange(actionId, userId, tenantId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/actions/{actionId}/reject")
  @Operation(summary = "Rechazar cambio de datos", description = "Rechaza una solicitud ESS")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Solicitud rechazada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> rejectAction(
      @PathVariable UUID actionId, @Valid @RequestBody RejectDataChangeRequest request) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    Long userId = SecurityUserContext.getUserId();
    managerSelfServiceUseCase.rejectDataChange(
        actionId, userId, request.rejectionReason(), tenantId);
    return ResponseEntity.ok().build();
  }
}
