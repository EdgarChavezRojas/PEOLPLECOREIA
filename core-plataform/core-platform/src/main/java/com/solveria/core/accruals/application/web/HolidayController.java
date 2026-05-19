package com.solveria.core.accruals.application.web;

import com.solveria.core.accruals.application.command.RegisterHolidayCommand;
import com.solveria.core.accruals.application.dto.RegisterHolidayRequest;
import com.solveria.core.accruals.application.usecase.RegisterHolidayUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@Tag(name = "Holidays", description = "API para la gestión administrativa de feriados (HR Super User)")
public class HolidayController {

    private final RegisterHolidayUseCase registerHolidayUseCase;

    private static final String DEFAULT_LOCATION = "Santa Cruz, Bolivia";

    @PostMapping
    @Operation(summary = "Registrar un nuevo feriado en el calendario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Feriado registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta acción")
    })
    public ResponseEntity<Void> registerHoliday(@RequestBody RegisterHolidayRequest request) {

        // TODO: En un futuro vamos a extraer la información de la location desde el JWT.

        RegisterHolidayCommand command = new RegisterHolidayCommand(
                request.holidayDate(),
                request.scope(),
                DEFAULT_LOCATION
        );

        registerHolidayUseCase.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}