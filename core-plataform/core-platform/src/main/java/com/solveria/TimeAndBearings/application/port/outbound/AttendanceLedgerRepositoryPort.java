package com.solveria.TimeAndBearings.application.port.outbound;

import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port: Persistence contract for AttendanceLedger (Aggregate 14).
 *
 * <p>Hexagonal Architecture: the domain depends on this interface. The JPA adapter in the
 * infrastructure layer implements it. BC-TM never writes to BC-01, BC-SCH, or BC-05 tables.
 */
public interface AttendanceLedgerRepositoryPort {

  /**
   * Finds the ledger for a given relationship and work date within a tenant. Returns empty if no
   * ledger exists for that day (new ledger must be created).
   *
   * @param tenantId Multi-tenant partition.
   * @param relationshipId BC-01 Relationship reference (opaque UUID).
   * @param workDate Day of the attendance ledger.
   * @return Optional containing the ledger if found, with all TimeEntry and TimeDeviationRecord
   *     loaded.
   */
  Optional<AttendanceLedger> findByRelationshipAndDate(
      UUID tenantId, UUID relationshipId, LocalDate workDate);

  /**
   * Finds a ledger by its primary key.
   *
   * @param ledgerId PK of the AttendanceLedger.
   * @return Optional containing the fully reconstituted aggregate.
   */
  Optional<AttendanceLedger> findById(UUID ledgerId);

  /**
   * Persists a new AttendanceLedger or updates an existing one. Implementation uses upsert
   * semantics via JPA merge / Spring Data save.
   *
   * @param ledger The aggregate to persist.
   * @return The persisted aggregate (may carry DB-generated metadata).
   */
  AttendanceLedger save(AttendanceLedger ledger);

  /**
   * Finds ledgers that contain PENDING deviations older than the cutoff time. Default returns empty
   * list; infrastructure adapters should override.
   */
  default List<AttendanceLedger> findLedgersWithExpiredPendingDeviations(
      UUID tenantId, LocalDateTime cutoffTime) {
    return List.of();
  }
}
