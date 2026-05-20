package com.solveria.scheduling.infrastructure.adapter;

import com.solveria.scheduling.application.port.outbound.AttendanceRecordRepositoryPort;
import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import com.solveria.scheduling.infrastructure.jpa.AttendanceRecordJpa;
import com.solveria.scheduling.infrastructure.mapper.AttendanceRecordMapper;
import com.solveria.scheduling.infrastructure.repository.AttendanceRecordJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceRecordRepositoryAdapter implements AttendanceRecordRepositoryPort {

  private final AttendanceRecordJpaRepository attendanceRecordJpaRepository;
  private final AttendanceRecordMapper attendanceRecordMapper;

  @Override
  public AttendanceRecord save(AttendanceRecord record) {
    AttendanceRecordJpa jpa = attendanceRecordMapper.toJpa(record);
    AttendanceRecordJpa saved = attendanceRecordJpaRepository.save(jpa);
    return attendanceRecordMapper.toDomain(saved);
  }

  @Override
  public Optional<AttendanceRecord> findById(UUID recordId) {
    return attendanceRecordJpaRepository
        .findByRecordId(recordId)
        .map(attendanceRecordMapper::toDomain);
  }

  @Override
  public Optional<AttendanceRecord> findByRelationshipIdAndDate(
      UUID relationshipId, LocalDate date) {
    return attendanceRecordJpaRepository
        .findByRelationshipIdAndWorkDate(relationshipId, date)
        .map(attendanceRecordMapper::toDomain);
  }

  @Override
  public List<AttendanceRecord> findOpenRecords() {
    return attendanceRecordJpaRepository.findOpenRecords().stream()
        .map(attendanceRecordMapper::toDomain)
        .collect(Collectors.toList());
  }
}
