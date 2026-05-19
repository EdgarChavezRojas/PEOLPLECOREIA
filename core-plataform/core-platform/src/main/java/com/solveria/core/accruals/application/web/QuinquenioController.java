package com.solveria.core.accruals.application.web;

import com.solveria.core.accruals.application.command.MarkQuinquenioPaidCommand;
import com.solveria.core.accruals.application.command.RequestQuinquenioPaymentCommand;
import com.solveria.core.accruals.application.dto.MarkQuinquenioPaidRequest;
import com.solveria.core.accruals.application.dto.RequestQuinquenioPaymentRequest;
import com.solveria.core.accruals.application.usecase.MarkQuinquenioPaidUseCase;
import com.solveria.core.accruals.application.usecase.RequestQuinquenioPaymentUseCase;
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
@RequestMapping("/api/v1/quinquenios")
@RequiredArgsConstructor
@Tag(name = "Quinquenios", description = "API para la gestión y pago de quinquenios (Beneficios a largo plazo)")
public class QuinquenioController {

    private final RequestQuinquenioPaymentUseCase requestPaymentUseCase;
    private final MarkQuinquenioPaidUseCase markPaidUseCase;

    private static final String DEFAULT_LOCATION = "Santa Cruz, Bolivia";

    @PostMapping("/request")
    @Operation(summary = "Solicitar pago de un quinquenio consolidado (ESS)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitud de pago generada"),
            @ApiResponse(responseCode = "400", description = "Quinquenio no disponible o ya solicitado")
    })
    public ResponseEntity<Void> requestPayment(@RequestBody RequestQuinquenioPaymentRequest request) {

        // TODO: En un futuro vamos a extraer la información de la location desde el JWT.

        RequestQuinquenioPaymentCommand command = new RequestQuinquenioPaymentCommand(
                request.relationshipId(),
                request.requestDate(),
                DEFAULT_LOCATION
        );

        requestPaymentUseCase.handle(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{relationshipId}/pay")
    @Operation(summary = "Registrar confirmación de pago de quinquenio (Finanzas)")
    @ApiResponse(responseCode = "200", description = "Quinquenio marcado como pagado")
    public ResponseEntity<Void> markAsPaid(
            @PathVariable UUID relationshipId,
            @RequestBody MarkQuinquenioPaidRequest request) {

        // TODO: En un futuro vamos a extraer la información de la location desde el JWT.

        MarkQuinquenioPaidCommand command = new MarkQuinquenioPaidCommand(
                relationshipId,
                request.paymentDate(),
                DEFAULT_LOCATION
        );

        markPaidUseCase.handle(command);

        return ResponseEntity.ok().build();
    }
}