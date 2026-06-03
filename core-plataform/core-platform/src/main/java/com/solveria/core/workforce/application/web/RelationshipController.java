package com.solveria.core.workforce.application.web;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import com.solveria.core.workforce.application.dto.RelationshipResponse;
import com.solveria.core.workforce.application.dto.TerminateRelationshipRequest;
import com.solveria.core.workforce.application.dto.UpdateEmployeeAcademicProfileRequest;
import com.solveria.core.workforce.application.dto.UpdateEmploymentConditionsRequest;
import com.solveria.core.workforce.application.dto.UpdateRelationshipStatusRequest;
import com.solveria.core.workforce.application.dto.webRequest.CreateRelationshipWebDto;
import com.solveria.core.workforce.application.usecase.*;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.solveria.core.shared.pagination.PageUtils;
import com.solveria.core.workforce.infrastructure.jpa.RelationshipJpa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
@Tag(
    name = "Employment Relationship",
    description = "Gestión de contratos y perfiles (Academic/Worker)")
public class RelationshipController {

  private final CreateRelationshipUseCase createRelationshipUseCase;
  private final UpdateEmploymentConditionsUseCase updateEmploymentConditionsUseCase;
  private final UpdateEmployeeAcademicProfileUseCase updateEmployeeAcademicProfileUseCase;
  private final TerminateRelationshipUseCase terminateRelationshipUseCase;
  private final ReactivateRelationshipUseCase reactivateRelationshipUseCase;
  private final ListRelationshipByPersonUseCase listRelationshipByPersonUseCase;
  private final UpdateRelationshipStatusUseCase updateRelationshipStatusUseCase;
  private final ListRelationshipsByTenantUseCase listRelationshipsByTenantUseCase;

  @PostMapping
  @Operation(
      summary = "Crear relación laboral",
      description =
          "Inicia un onboarding y asigna perfiles específicos (Docente o Administrativo).")
  public ResponseEntity<RelationshipResponse> create(
      @Valid @RequestBody CreateRelationshipWebDto request) {
    UUID tenantUuid = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(createRelationshipUseCase.execute(request.toCommand(tenantUuid)));
  }

  @PatchMapping("/{relationshipId}/conditions")
  @Operation(
      summary = "Actualizar condiciones laborales",
      description = "Actualiza la condicion laboral de la relacion.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Condiciones actualizadas"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Relacion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> updateConditions(
      @PathVariable UUID relationshipId,
      @Valid @RequestBody UpdateEmploymentConditionsRequest request) {
    updateEmploymentConditionsUseCase.execute(relationshipId, request.getCondition());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{relationshipId}/academic-profile")
  @Operation(
      summary = "Actualizar perfil academico",
      description = "Actualiza el rango academico de la relacion laboral.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Perfil academico actualizado"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Relacion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> updateAcademicProfile(
      @PathVariable UUID relationshipId,
      @Valid @RequestBody UpdateEmployeeAcademicProfileRequest request) {
    UUID tenantUuid = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    updateEmployeeAcademicProfileUseCase.execute(
        relationshipId, tenantUuid, String.valueOf(request.getNewRank()));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{relationshipId}/terminate")
  @Operation(
      summary = "Terminar relacion laboral",
      description = "Finaliza la relacion laboral y registra el motivo.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Relacion terminada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Relacion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> terminate(
      @PathVariable UUID relationshipId, @Valid @RequestBody TerminateRelationshipRequest request) {
    terminateRelationshipUseCase.execute(relationshipId, request.getReason());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{relationshipId}/reactivate")
  @Operation(
      summary = "Reactivar relacion laboral",
      description = "Reactiva una relacion laboral previamente terminada.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Relacion reactivada"),
    @ApiResponse(responseCode = "404", description = "Relacion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> reactivate(@PathVariable UUID relationshipId) {
    reactivateRelationshipUseCase.execute(relationshipId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  @Operation(
      summary = "Encontrar la relacion laboral por usuario",
      description =
          "Encuentra la relacion laboral por usuario y devuelve una lista de sus relaciones")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Relacion reactivada"),
    @ApiResponse(responseCode = "404", description = "Relacion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<List<Relationship>> getByPersonId() {
    Long userId = SecurityUserContext.getUserId();
    return ResponseEntity.status(HttpStatus.OK)
        .body(listRelationshipByPersonUseCase.execute(userId));
  }

  @PatchMapping("/{relationshipId}/status")
  @Operation(
      summary = "Actualizar estado de relacion laboral",
      description = "Actualiza el estado de la relacion (ACTIVE o SUSPENDED).")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Estado actualizado"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Relacion no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> updateStatus(
      @PathVariable UUID relationshipId, @RequestParam("status") RelationshipStatus status) {
    updateRelationshipStatusUseCase.execute(
        relationshipId, UpdateRelationshipStatusRequest.builder().status(status).build());
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(
      summary = "Listar relaciones laborales/académicas del tenant",
      description = "Obtiene una lista paginada de todas las relaciones del tenant actual.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Listado paginado obtenido exitosamente"),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Page<RelationshipResponse>> list(Pageable pageable) {
    Pageable sanitized = PageUtils.sanitize(pageable, RelationshipJpa.class);
    return ResponseEntity.ok(listRelationshipsByTenantUseCase.execute(sanitized));
  }
}
