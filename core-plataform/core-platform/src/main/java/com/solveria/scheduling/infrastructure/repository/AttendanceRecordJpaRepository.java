package com.solveria.scheduling.infrastructure.repository;

import com.solveria.scheduling.infrastructure.jpa.AttendanceRecordJpa;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecordJpa, Long> {
    
    Optional<AttendanceRecordJpa> findByRelationshipIdAndWorkDate(UUID relationshipId, LocalDate workDate);

    @Query("SELECT a FROM AttendanceRecordJpa a WHERE a.isClosed = false")
    List<AttendanceRecordJpa> findOpenRecords();

    Optional<AttendanceRecordJpa> findByRecordId(UUID recordId);
}
