package com.solveria.scheduling.infrastructure.repository;

import com.solveria.scheduling.domain.model.ar.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecord, UUID> {
    
    Optional<AttendanceRecord> findByRelationshipIdAndWorkDate(UUID relationshipId, LocalDate workDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.isClosed = false")
    List<AttendanceRecord> findOpenRecords();
}
