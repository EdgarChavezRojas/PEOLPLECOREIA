package com.solveria.ai.application.service;

import com.solveria.ai.application.dto.EmployeeHandoffRecordDto;
import com.solveria.ai.application.dto.PayrollHandoffEventDto;
import com.solveria.ai.application.port.in.IngestAttendancePeriodUseCase;
import com.solveria.ai.application.port.out.VectorIngestionPort;
import java.util.HashMap;
import java.util.Map;

public class IngestAttendancePeriodService implements IngestAttendancePeriodUseCase {

    private final VectorIngestionPort vectorIngestionPort;

    public IngestAttendancePeriodService(VectorIngestionPort vectorIngestionPort) {
        this.vectorIngestionPort = vectorIngestionPort;
    }

    @Override
    public void ingest(PayrollHandoffEventDto event) {
        if (event == null || event.records() == null) {
            return;
        }

        for (EmployeeHandoffRecordDto record : event.records()) {
            String text =
                    "Reporte de Asistencia del periodo desde "
                            + event.periodStart()
                            + " hasta "
                            + event.periodEnd()
                            + ". Empresa/Tenant: "
                            + event.tenantId()
                            + ". El empleado con ID de relación (relationship_id) '"
                            + record.relationshipId()
                            + "' acumuló un total de "
                            + record.unjustifiedAbsences()
                            + " faltas injustificadas (ausencias sin justificar / NO_SHOW) durante este mes de consolidación.";

            Map<String, Object> metadata = getStringObjectMap(event, record);

            vectorIngestionPort.persistTextChunk(
                    text, event.tenantId(), "asistencia-resumenes", metadata);
        }
    }

    private Map<String, Object> getStringObjectMap(PayrollHandoffEventDto event, EmployeeHandoffRecordDto record) {
        String yearMonth = "UNKNOW";
        if (event.periodStart() != null && event.periodStart().length() >= 7) {
            yearMonth = event.periodStart().substring(0, 7);
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("periodStart", event.periodStart());
        metadata.put("periodEnd", event.periodEnd());
        metadata.put("orgUnitId", event.orgUnitId());
        metadata.put("relationshipId", record.relationshipId());
        metadata.put("unjustifiedAbsences", record.unjustifiedAbsences());
        metadata.put("yearMonth", yearMonth);
        return metadata;
    }
}
