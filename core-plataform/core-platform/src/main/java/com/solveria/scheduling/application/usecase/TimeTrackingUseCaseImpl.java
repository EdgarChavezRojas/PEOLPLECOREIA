package com.solveria.scheduling.application.usecase;

import com.solveria.scheduling.application.port.inbound.TimeTrackingUseCase;
import com.solveria.scheduling.application.port.outbound.AttendanceRecordRepositoryPort;
import com.solveria.scheduling.domain.exception.DomainRuleViolationException;
import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import com.solveria.scheduling.domain.model.entity.TimeEntry;
import com.solveria.scheduling.domain.model.enums.PunchType;
import com.solveria.scheduling.domain.model.vo.GeoValidation;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeTrackingUseCaseImpl implements TimeTrackingUseCase {

    private final AttendanceRecordRepositoryPort attendanceRecordRepositoryPort;

    @Override
    @Transactional
    public void registerPunch(UUID relationshipId, LocalDateTime punchTime, PunchType punchType, String deviceId, GeoValidation geoValidation) {
        // Validación Geocerca
        if (geoValidation != null && Boolean.FALSE.equals(geoValidation.isWithinFence())) {
            throw new DomainRuleViolationException("La marcación está fuera de la geocerca permitida.");
        }

        AttendanceRecord record = attendanceRecordRepositoryPort
            .findByRelationshipIdAndDate(relationshipId, punchTime.toLocalDate())
            .orElseGet(() -> new AttendanceRecord(relationshipId, punchTime.toLocalDate()));

        TimeEntry entry = new TimeEntry(punchTime, punchType, deviceId, "SYSTEM", geoValidation);
        record.addEntry(entry);

        attendanceRecordRepositoryPort.save(record);
    }
}
