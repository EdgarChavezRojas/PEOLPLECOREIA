package com.solveria.payroll.application.web;

import com.solveria.payroll.application.port.inbound.GenerateDispersionFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dispersions")
@Tag(
    name = "Bank Dispersions",
    description = "Endpoints para la generación de archivos de dispersión bancaria")
public class BankDispersionController {

  private final GenerateDispersionFileUseCase generateDispersionFileUseCase;

  public BankDispersionController(GenerateDispersionFileUseCase generateDispersionFileUseCase) {
    this.generateDispersionFileUseCase = generateDispersionFileUseCase;
  }

  @PostMapping("/generate/{runId}")
  @Operation(
      summary = "Generar archivo de dispersión",
      description = "Genera el archivo del banco para una planilla cerrada.")
  public ResponseEntity<Void> generateDispersionFile(
      @PathVariable UUID runId, @RequestHeader("X-Tenant-ID") String tenantId) {
    //        generateDispersionFileUseCase.execute(runId, tenantId);
    return ResponseEntity.ok().build();
  }
}
