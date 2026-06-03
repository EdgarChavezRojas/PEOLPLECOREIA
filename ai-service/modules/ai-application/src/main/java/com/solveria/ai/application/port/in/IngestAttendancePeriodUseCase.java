package com.solveria.ai.application.port.in;

import com.solveria.ai.application.dto.PayrollHandoffEventDto;

public interface IngestAttendancePeriodUseCase {
    void ingest(PayrollHandoffEventDto event);
}
