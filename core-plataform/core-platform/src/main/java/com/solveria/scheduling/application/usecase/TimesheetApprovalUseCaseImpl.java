package com.solveria.scheduling.application.usecase;

import com.solveria.scheduling.application.port.inbound.TimesheetApprovalUseCase;
import com.solveria.scheduling.application.port.outbound.AttendanceRecordRepositoryPort;
import com.solveria.scheduling.application.port.outbound.SchedulingEventOutboxPort;
import com.solveria.scheduling.domain.event.AttendanceReadyForPayrollEvent;
import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimesheetApprovalUseCaseImpl implements TimesheetApprovalUseCase {

    private final AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;
    private final SchedulingEventOutboxPort eventOutboxPort;

    @Override
    @Transactional
    public void runDailyReconciliation() {
        List<AttendanceRecord> openRecords = attendanceRecordRepositoryPort.findOpenRecords();

        for (AttendanceRecord record : openRecords) {
            try {
                // Aquí se integraría con TimeTrackingValidationService para calcular desviaciones reales.
                // Como ejemplo, si tiene un número par de entradas sin desviaciones pendientes, se cierra.
                
                record.closeRecord();
                attendanceRecordRepositoryPort.save(record);

                // Emite evento para Payroll (BC 05)
                eventOutboxPort.publish(new AttendanceReadyForPayrollEvent(
                    record.getRecordId(), 
                    record.getRelationshipId(), 
                    Instant.now()
                ));
            } catch (Exception e) {
                // Si la validación falla (ej. entradas impares y sin desviación aprobada), 
                // se mantiene abierto o se marca como PENDING_REVIEW.
                // Log e.getMessage()
            }
        }
    }
}
