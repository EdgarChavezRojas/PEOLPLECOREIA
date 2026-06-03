package com.solveria.ai.api.controller;

import com.solveria.ai.application.dto.PayrollHandoffEventDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/test")
@Tag(name = "Módulo de Pruebas de IA", description = "Endpoints temporales para simulación e inyección de eventos de dominio")
public class TestEventController {

    private final ApplicationEventPublisher eventPublisher;

    public TestEventController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/trigger-closure")
    @Operation(
            summary = "Simular disparo del evento ATTENDANCE_PERIOD_CLOSED",
            description = "Inyecta un payload PayrollHandoffEventDto síncronamente en el bus de Spring para que el pipeline del RAG lo indexe de forma asíncrona.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Evento publicado exitosamente en el bus interno del sistema."
                    )
            }
    )
    public ResponseEntity<String> triggerEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload con datos semilla de asistencia consistentes con las migraciones Flyway de Solveria.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PayrollHandoffEventDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Semilla Coherente (Solveria Mayo 2026)",
                                            summary = "JSON alineado con los inserts de Flyway",
                                            value = """
                    {
                      "tenantId": "e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b",
                      "orgUnitId": "b2b00000-0000-4000-8000-000000000002",
                      "periodStart": "2026-05-01",
                      "periodEnd": "2026-05-31",
                      "records": [
                        {
                          "relationshipId": "9f9a6f1a-2a1d-4a61-9a1b-200000000002",
                          "unjustifiedAbsences": 4,
                          "regularHoursTotal": 160.0,
                          "overtimeHoursTotal": 8.5
                        },
                        {
                          "relationshipId": "9f9a6f1a-2a1d-4a61-9a1b-200000000003",
                          "unjustifiedAbsences": 0,
                          "regularHoursTotal": 168.0,
                          "overtimeHoursTotal": 14.0
                        }
                      ]
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Semilla Histórica (Solveria Abril 2026)",
                                            summary = "JSON con periodo anterior y variación de ausencias",
                                            value = """
                    {
                      "tenantId": "e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b",
                      "orgUnitId": "b2b00000-0000-4000-8000-000000000002",
                      "periodStart": "2026-04-01",
                      "periodEnd": "2026-04-30",
                      "records": [
                        {
                          "relationshipId": "9f9a6f1a-2a1d-4a61-9a1b-200000000002",
                          "unjustifiedAbsences": 1,
                          "regularHoursTotal": 160.0,
                          "overtimeHoursTotal": 6.0
                        },
                        {
                          "relationshipId": "9f9a6f1a-2a1d-4a61-9a1b-200000000003",
                          "unjustifiedAbsences": 0,
                          "regularHoursTotal": 168.0,
                          "overtimeHoursTotal": 12.0
                        }
                      ]
                    }
                    """
                                    )
                        }
                )
            )
            @RequestBody PayrollHandoffEventDto payload
    ) {
        eventPublisher.publishEvent(payload);
        return ResponseEntity.ok("Evento de cierre de asistencia publicado en el bus interno con éxito. Verifique los logs asíncronos.");
    }
}