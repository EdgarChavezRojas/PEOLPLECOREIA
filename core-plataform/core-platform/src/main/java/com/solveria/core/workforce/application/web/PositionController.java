package com.solveria.core.workforce.application.web;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.AssignPersonToPositionRequest;
import com.solveria.core.workforce.application.dto.CreatePositionRequest;
import com.solveria.core.workforce.application.dto.PositionResponse;
import com.solveria.core.workforce.application.dto.UpdatePositionBudgetRequest;
import com.solveria.core.workforce.application.usecase.AssignPersonToPositionUseCase;
import com.solveria.core.workforce.application.usecase.CreatePositionUseCase;
import com.solveria.core.workforce.application.usecase.ListPositionsUseCase;
import com.solveria.core.workforce.application.usecase.UpdatePositionBudgetUseCase;
import com.solveria.core.workforce.application.usecase.VacatePositionUseCase;
import com.solveria.core.workforce.domain.model.vo.PositionStatus;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
@Tag(name = "Position", description = "Gestión de cargos y presupuesto de plazas (Headcount)")
public class PositionController {

  private final CreatePositionUseCase createPositionUseCase;
  private final AssignPersonToPositionUseCase assignPersonToPositionUseCase;
  private final VacatePositionUseCase vacatePositionUseCase;
  private final UpdatePositionBudgetUseCase updatePositionBudgetUseCase;
  private final ListPositionsUseCase listPositionsUseCase;

  @GetMapping
  @Operation(
      summary = "Listar posiciones",
      description = "Obtiene un listado paginado de posiciones. Permite filtrar por status.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Listado paginado obtenido"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Page<PositionResponse>> list(
      Pageable pageable, @RequestParam(required = false) PositionStatus status) {
    return ResponseEntity.ok(listPositionsUseCase.execute(pageable, status));
  }

  @PostMapping
  @Operation(
      summary = "Crear posición (Cargo)",
      description = "Registra una plaza disponible asociada a una unidad organizacional.")
  public ResponseEntity<PositionResponse> create(
      @Valid @RequestBody CreatePositionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(createPositionUseCase.execute(request));
  }

  @PostMapping("/{positionId}/assign")
  @Operation(
      summary = "Asignar persona a posicion",
      description = "Asigna una persona a una posicion vacante.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Persona asignada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Posicion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> assign(
      @PathVariable UUID positionId, @Valid @RequestBody AssignPersonToPositionRequest request) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    assignPersonToPositionUseCase.execute(positionId, tenantId, request.getPersonId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{positionId}/vacate")
  @Operation(
      summary = "Vaciar posicion",
      description = "Libera la posicion y actualiza su estado de disponibilidad.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Posicion liberada"),
    @ApiResponse(responseCode = "404", description = "Posicion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> vacate(@PathVariable UUID positionId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    vacatePositionUseCase.execute(positionId, tenantId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{positionId}/budget")
  @Operation(
      summary = "Actualizar presupuesto de posicion",
      description = "Marca una posicion como presupuestada o no presupuestada.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Presupuesto actualizado"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Posicion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> updateBudget(
      @PathVariable UUID positionId, @Valid @RequestBody UpdatePositionBudgetRequest request) {
    updatePositionBudgetUseCase.execute(positionId, request.getIsBudgeted());
    return ResponseEntity.noContent().build();
  }
}
