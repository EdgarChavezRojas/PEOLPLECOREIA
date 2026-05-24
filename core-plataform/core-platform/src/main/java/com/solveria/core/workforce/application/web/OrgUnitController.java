package com.solveria.core.workforce.application.web;

import com.solveria.core.workforce.application.dto.CreateOrgUnitRequest;
import com.solveria.core.workforce.application.dto.MoveOrgUnitRequest;
import com.solveria.core.workforce.application.dto.OrgUnitResponse;
import com.solveria.core.workforce.application.dto.RelocateOrgUnitRequest;
import com.solveria.core.workforce.application.usecase.CreateOrgUnitUseCase;
import com.solveria.core.workforce.application.usecase.ListOrgUnitsUseCase;
import com.solveria.core.workforce.application.usecase.MoveOrgUnitUseCase;
import com.solveria.core.workforce.application.usecase.RelocateOrgUnitUseCase;
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
@RequestMapping("/api/v1/org-units")
@RequiredArgsConstructor
@Tag(
    name = "Organization Unit",
    description = "Gestión de departamentos, sucursales y centros de costo")
public class OrgUnitController {

  private final CreateOrgUnitUseCase createOrgUnitUseCase;
  private final MoveOrgUnitUseCase moveOrgUnitUseCase;
  private final RelocateOrgUnitUseCase relocateOrgUnitUseCase;
  private final ListOrgUnitsUseCase listOrgUnitsUseCase;

  @GetMapping
  @Operation(
      summary = "Listar unidades organizativas",
      description = "Obtiene un listado paginado de unidades organizativas del tenant actual.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Listado paginado obtenido"),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Page<OrgUnitResponse>> list(Pageable pageable) {
    return ResponseEntity.ok(listOrgUnitsUseCase.execute(pageable));
  }

  @PostMapping
  @Operation(
      summary = "Crear unidad organizativa Raíz",
      description =
          "Crea el nodo principal (Root) de la empresa/institución para el tenant actual.")
  public ResponseEntity<OrgUnitResponse> createRoot(
      @Valid @RequestBody CreateOrgUnitRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(createOrgUnitUseCase.executeRoot(request));
  }

  @PostMapping("/{parentId}/children")
  @Operation(
      summary = "Crear unidad organizativa Hija",
      description =
          "Crea un nuevo departamento o sucursal y lo vincula jerárquicamente a la unidad padre especificada.")
  public ResponseEntity<OrgUnitResponse> createChild(
      @PathVariable UUID parentId, @Valid @RequestBody CreateOrgUnitRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(createOrgUnitUseCase.executeChild(request, parentId));
  }

  @PatchMapping("/{unitId}/move")
  @Operation(
      summary = "Mover unidad organizativa",
      description = "Reasigna la unidad a un nuevo padre (o null para raiz).")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Unidad movida"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Unidad no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> move(
      @PathVariable UUID unitId, @Valid @RequestBody MoveOrgUnitRequest request) {
    moveOrgUnitUseCase.execute(unitId, request.getNewParentId());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{unitId}/relocate")
  @Operation(
      summary = "Reubicar unidad organizativa",
      description = "Actualiza la extension geografica de la unidad organizativa.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Unidad reubicada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Unidad no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> relocate(
      @PathVariable UUID unitId, @Valid @RequestBody RelocateOrgUnitRequest request) {
    relocateOrgUnitUseCase.execute(unitId, request.getGeoExtension());
    return ResponseEntity.noContent().build();
  }
}
