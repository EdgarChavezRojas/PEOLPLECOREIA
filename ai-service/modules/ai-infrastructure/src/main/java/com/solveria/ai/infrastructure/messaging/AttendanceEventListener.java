package com.solveria.ai.infrastructure.messaging;

import com.solveria.ai.application.dto.PayrollHandoffEventDto;
import com.solveria.ai.application.port.in.IngestAttendancePeriodUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AttendanceEventListener {

    private static final Logger log = LoggerFactory.getLogger(AttendanceEventListener.class);
    private final IngestAttendancePeriodUseCase ingestAttendancePeriodUseCase;

    public AttendanceEventListener(IngestAttendancePeriodUseCase ingestAttendancePeriodUseCase) {
        this.ingestAttendancePeriodUseCase = ingestAttendancePeriodUseCase;
    }

    @EventListener
    @Async
    public void handleAttendancePeriodClosed(PayrollHandoffEventDto event) {
        if (event == null) {
            log.warn("event=ATTENDANCE_INGEST_IGNORE reason=null_event");
            return;
        }
        log.info(
                "event=ATTENDANCE_INGEST_START tenantId={} orgUnitId={}",
                event.tenantId(),
                event.orgUnitId());
        try {
            ingestAttendancePeriodUseCase.ingest(event);
            log.info(
                    "event=ATTENDANCE_INGEST_SUCCESS tenantId={} orgUnitId={}",
                    event.tenantId(),
                    event.orgUnitId());
        } catch (Exception e) {
            log.error(
                    "event=ATTENDANCE_INGEST_FAILURE tenantId={} orgUnitId={} error={}",
                    event.tenantId(),
                    event.orgUnitId(),
                    e.getMessage(),
                    e);
        }
    }
}
