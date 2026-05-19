package com.solveria.core.accruals.application.web;

import com.solveria.core.accruals.application.command.ApproveLeaveCommand;
import com.solveria.core.accruals.application.command.RejectLeaveCommand;
import com.solveria.core.accruals.application.command.RequestLeaveCommand;
import com.solveria.core.accruals.application.dto.RequestLeaveRequest;
import com.solveria.core.accruals.application.dto.ReviewLeaveRequest;
import com.solveria.core.accruals.application.usecase.ApproveLeaveUseCase;
import com.solveria.core.accruals.application.usecase.RejectLeaveUseCase;
import com.solveria.core.accruals.application.usecase.RequestLeaveUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vacations")
@RequiredArgsConstructor
@Tag(name = "Vacations", description = "API para solicitudes y aprobaciones de vacaciones (ESS / MSS)")
public class VacationController {

    private final RequestLeaveUseCase requestLeaveUseCase;
    private final ApproveLeaveUseCase approveLeaveUseCase;
    private final RejectLeaveUseCase rejectLeaveUseCase;

    private static final String DEFAULT_LOCATION = "Santa Cruz, Bolivia";

    @PostMapping("/request")
    @Operation(summary = "Solicitar vacaciones (Flujo ESS)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitud creada y pendiente de aprobación"),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente o fechas inválidas")
    })
    public ResponseEntity<Void> requestLeave(@RequestBody RequestLeaveRequest request) {

        // TODO: En un futuro vamos a extraer la información de la location desde el JWT.

        RequestLeaveCommand command = new RequestLeaveCommand(
                request.balanceId(),
                request.startDate(),
                request.endDate(),
                DEFAULT_LOCATION
        );

        requestLeaveUseCase.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{transactionId}/approve")
    @Operation(summary = "Aprobar solicitud de vacaciones (Flujo MSS)")
    @ApiResponse(responseCode = "200", description = "Solicitud aprobada exitosamente")
    public ResponseEntity<Void> approveLeave(
            @PathVariable UUID transactionId,
            @RequestBody ReviewLeaveRequest request) {

        // TODO: En un futuro vamos a extraer la información de la location desde el JWT.

        ApproveLeaveCommand command = new ApproveLeaveCommand(
                request.balanceId(),
                transactionId,
                DEFAULT_LOCATION
        );

        approveLeaveUseCase.handle(command);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{transactionId}/reject")
    @Operation(summary = "Rechazar solicitud de vacaciones (Flujo MSS)")
    @ApiResponse(responseCode = "200", description = "Solicitud rechazada")
    public ResponseEntity<Void> rejectLeave(
            @PathVariable UUID transactionId,
            @RequestBody ReviewLeaveRequest request) {

        // TODO: En un futuro vamos a extraer la información de la location desde el JWT.

        RejectLeaveCommand command = new RejectLeaveCommand(
                request.balanceId(),
                transactionId,
                DEFAULT_LOCATION
        );

        rejectLeaveUseCase.handle(command);

        return ResponseEntity.ok().build();
    }
}