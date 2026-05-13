package com.solveria.core.legal.application.web;
import com.solveria.core.legal.application.dto.GenerateContractEvidenceRequest;
import com.solveria.core.legal.application.dto.UpdateLegalThresholdRequest;
import com.solveria.core.legal.application.usecase.GenerateContractEvidenceUseCase;
import com.solveria.core.legal.application.usecase.ScanExpiringContractsUseCase;
import com.solveria.core.legal.application.usecase.UpdateLegalThresholdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/legal")
@Tag(name = "Legal & Compliance", description = "API para la gestión de normativas legales, evidencias de contratos y reglas laborales")
public class LegalComplianceController {

    private final GenerateContractEvidenceUseCase generateContractEvidenceUseCase;
    private final ScanExpiringContractsUseCase scanExpiringContractsUseCase;
    private final UpdateLegalThresholdUseCase updateLegalThresholdUseCase;

    public LegalComplianceController(
            GenerateContractEvidenceUseCase generateContractEvidenceUseCase,
            ScanExpiringContractsUseCase scanExpiringContractsUseCase,
            UpdateLegalThresholdUseCase updateLegalThresholdUseCase) {
        this.generateContractEvidenceUseCase = generateContractEvidenceUseCase;
        this.scanExpiringContractsUseCase = scanExpiringContractsUseCase;
        this.updateLegalThresholdUseCase = updateLegalThresholdUseCase;
    }

    // -------------------------------------------------------------------
    // 1. Caso de Uso: Actualizar Umbrales Legales
    // -------------------------------------------------------------------
    @PutMapping("/thresholds")
    @Operation(summary = "Actualiza un umbral legal", description = "Modifica el valor de una regla laboral (ej. Salario Mínimo Nacional).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Umbral actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Bad Request: El nuevo valor es menor al actual o formato inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized: No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden: No tiene permisos de administrador HR", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found: La regla legal (ruleId) no existe", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Fallo inesperado en el servidor", content = @Content)
    })
    public ResponseEntity<Void> updateLegalThreshold(
            @Parameter(description = "ID de la regla legal a actualizar", required = true)
            @RequestBody UpdateLegalThresholdRequest request) {

        updateLegalThresholdUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------
    // 2. Caso de Uso: Generar Evidencia de Contrato WORM
    // -------------------------------------------------------------------
    @PostMapping("/contracts/{contractId}/evidence")
    @Operation(summary = "Genera evidencia legal (WORM)", description = "Recopila los datos del contrato y genera un registro inmutable en el Digital Kardex.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evidencia generada y registrada con éxito"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Datos faltantes para generar la evidencia", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized: No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden: Error de TenantMismatch (El contrato pertenece a otra empresa)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found: Contrato no encontrado (ContractNotFoundException)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict: La evidencia del contrato ya fue generada previamente o falta el addendum base", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Fallo al comunicarse con el módulo Kardex/Dossier", content = @Content)
    })
    public ResponseEntity<Object> generateContractEvidence(
            @Parameter(description = "ID del contrato", required = true)
            @RequestBody GenerateContractEvidenceRequest request) {

        var response = generateContractEvidenceUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------
    // 3. Caso de Uso: Escaneo Manual de Contratos
    // -------------------------------------------------------------------
    @PostMapping("/contracts/scan-expiring")
    @Operation(summary = "Ejecutar escaneo de contratos (Tácita Reconducción)", description = "Dispara manualmente el escaneo de contratos próximos a expirar.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Accepted: Proceso de escaneo iniciado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden: Solo usuarios del sistema pueden ejecutar esta acción", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: Fallo al ejecutar el job", content = @Content)
    })
    public ResponseEntity<Void> triggerExpiringContractsScan() {

        scanExpiringContractsUseCase.execute();
        return ResponseEntity.accepted().build();
    }
}