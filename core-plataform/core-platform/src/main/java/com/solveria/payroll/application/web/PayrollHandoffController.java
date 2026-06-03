package com.solveria.payroll.application.web;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.payroll.application.port.inbound.AttendanceHandoffUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payroll-handoff")
@Tag(name = "Payroll Handoff", description = "Endpoints para la integración con Time and Bearings")
public class PayrollHandoffController {

  private final AttendanceHandoffUseCase attendanceHandoffUseCase;

  public PayrollHandoffController(AttendanceHandoffUseCase attendanceHandoffUseCase) {
    this.attendanceHandoffUseCase = attendanceHandoffUseCase;
  }

  @PostMapping("/sync-attendance")
  @Operation(
      summary = "Sincronizar asistencia",
      description = "Ejecuta la sincronización manual de asistencia para un periodo.")
  public ResponseEntity<Void> syncAttendance(@RequestParam UUID periodId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    attendanceHandoffUseCase.manualSync(periodId, tenantId);
    return ResponseEntity.ok().build();
  }
}
