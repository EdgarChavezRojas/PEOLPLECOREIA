package com.solveria.core.workforce.application.web;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.RelationshipResponse;
import com.solveria.core.workforce.application.dto.TerminateRelationshipRequest;
import com.solveria.core.workforce.application.dto.UpdateEmployeeAcademicProfileRequest;
import com.solveria.core.workforce.application.dto.UpdateEmploymentConditionsRequest;
import com.solveria.core.workforce.application.dto.webRequest.CreateRelationshipWebDto;
import com.solveria.core.workforce.application.usecase.CreateRelationshipUseCase;
import com.solveria.core.workforce.application.usecase.ReactivateRelationshipUseCase;
import com.solveria.core.workforce.application.usecase.TerminateRelationshipUseCase;
import com.solveria.core.workforce.application.usecase.UpdateEmployeeAcademicProfileUseCase;
import com.solveria.core.workforce.application.usecase.UpdateEmploymentConditionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    updateEmployeeAcademicProfileUseCase.execute(relationshipId, tenantUuid, request.getNewRank());
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
}
