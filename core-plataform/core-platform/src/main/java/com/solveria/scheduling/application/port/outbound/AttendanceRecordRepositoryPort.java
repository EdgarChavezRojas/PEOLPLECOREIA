package com.solveria.scheduling.application.port.outbound;

import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepositoryPort {
  AttendanceRecord save(AttendanceRecord record);

  Optional<AttendanceRecord> findById(UUID recordId);

  Optional<AttendanceRecord> findByRelationshipIdAndDate(UUID relationshipId, LocalDate date);

  List<AttendanceRecord> findOpenRecords();
}
