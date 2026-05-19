package com.solveria.core.workforce.application.web;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.CreateRelationshipRequest;
import com.solveria.core.workforce.application.dto.RelationshipResponse;
import com.solveria.core.workforce.application.dto.webRequest.CreateRelationshipWebDto;
import com.solveria.core.workforce.application.usecase.CreateRelationshipUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
@Tag(name = "Employment Relationship", description = "Gestión de contratos y perfiles (Academic/Worker)")
public class RelationshipController {

    private final CreateRelationshipUseCase createRelationshipUseCase;

    @PostMapping
    @Operation(summary = "Crear relación laboral",
            description = "Inicia un onboarding y asigna perfiles específicos (Docente o Administrativo).")
    public ResponseEntity<RelationshipResponse> create(@Valid @RequestBody CreateRelationshipWebDto request) {
        UUID tenantUuid = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createRelationshipUseCase.execute(request.toCommand(tenantUuid)));
    }
}