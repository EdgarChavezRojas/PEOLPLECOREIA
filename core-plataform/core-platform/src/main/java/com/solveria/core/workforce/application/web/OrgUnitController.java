package com.solveria.core.workforce.application.web;

import com.solveria.core.workforce.application.dto.CreateOrgUnitRequest;
import com.solveria.core.workforce.application.dto.OrgUnitResponse;
import com.solveria.core.workforce.application.usecase.CreateOrgUnitUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org-units")
@RequiredArgsConstructor
@Tag(name = "Organization Unit", description = "Gestión de departamentos, sucursales y centros de costo")
public class OrgUnitController {

    private final CreateOrgUnitUseCase createOrgUnitUseCase;

    @PostMapping
    @Operation(
            summary = "Crear unidad organizativa Raíz",
            description = "Crea el nodo principal (Root) de la empresa/institución para el tenant actual.")
    public ResponseEntity<OrgUnitResponse> createRoot(@Valid @RequestBody CreateOrgUnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrgUnitUseCase.executeRoot(request));
    }

    @PostMapping("/{parentId}/children")
    @Operation(
            summary = "Crear unidad organizativa Hija",
            description = "Crea un nuevo departamento o sucursal y lo vincula jerárquicamente a la unidad padre especificada.")
    public ResponseEntity<OrgUnitResponse> createChild(
            @PathVariable UUID parentId,
            @Valid @RequestBody CreateOrgUnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrgUnitUseCase.executeChild(request, parentId));
    }
}