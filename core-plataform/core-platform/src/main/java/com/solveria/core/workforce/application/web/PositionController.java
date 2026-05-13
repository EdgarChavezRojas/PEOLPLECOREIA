package com.solveria.core.workforce.application.web;

import com.solveria.core.workforce.application.dto.CreatePositionRequest;
import com.solveria.core.workforce.application.dto.PositionResponse;
import com.solveria.core.workforce.application.usecase.CreatePositionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
@Tag(name = "Position", description = "Gestión de cargos y presupuesto de plazas (Headcount)")
public class PositionController {

    private final CreatePositionUseCase createPositionUseCase;

    @PostMapping
    @Operation(summary = "Crear posición (Cargo)",
            description = "Registra una plaza disponible asociada a una unidad organizacional.")
    public ResponseEntity<PositionResponse> create(@Valid @RequestBody CreatePositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createPositionUseCase.execute(request));
    }
}