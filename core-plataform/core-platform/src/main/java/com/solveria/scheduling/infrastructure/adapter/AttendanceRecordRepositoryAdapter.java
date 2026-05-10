package com.solveria.scheduling.infrastructure.adapter;

import com.solveria.scheduling.application.port.outbound.AttendanceRecordRepositoryPort;
import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import com.solveria.scheduling.infrastructure.repository.AttendanceRecordJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceRecordRepositoryAdapter implements AttendanceRecordRepositoryPort {

    private final AttendanceRecordJpaRepository attendanceRecordJpaRepository;

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        return attendanceRecordJpaRepository.save(record);
    }

    @Override
    public Optional<AttendanceRecord> findById(UUID recordId) {
        return attendanceRecordJpaRepository.findById(recordId);
    }

    @Override
    public Optional<AttendanceRecord> findByRelationshipIdAndDate(UUID relationshipId, LocalDate date) {
        return attendanceRecordJpaRepository.findByRelationshipIdAndWorkDate(relationshipId, date);
    }

    @Override
    public List<AttendanceRecord> findOpenRecords() {
        return attendanceRecordJpaRepository.findOpenRecords();
    }
}
