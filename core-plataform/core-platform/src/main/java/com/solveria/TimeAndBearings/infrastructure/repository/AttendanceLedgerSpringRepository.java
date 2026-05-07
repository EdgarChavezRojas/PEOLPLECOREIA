package com.solveria.TimeAndBearings.infrastructure.repository;

import com.solveria.TimeAndBearings.infrastructure.jpa.AttendanceLedgerJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for {@link AttendanceLedgerJpa}.
 *
 * <p>The multi-tenant {@code tenantFilter} defined in {@link com.solveria.core.shared.base.BaseEntity}
 * applies automatically at the Hibernate session level. Individual queries also bind
 * {@code tenantId} explicitly for clarity.
 *
 * <p>Uses {@code UUID} as the JPA entity identifier to align with the domain model's
 * ledger_id (not the surrogate {@code Long id} from BaseEntity).
 */
@Repository
public interface AttendanceLedgerSpringRepository
        extends JpaRepository<AttendanceLedgerJpa, UUID> {

    /**
     * Returns the unique ledger for a collaborator on a specific work date within a tenant.
     * The UNIQUE CONSTRAINT on (relationship_id, work_date, tenant_id) guarantees
     * at most one result — enforces the business rule of one ledger per person per day.
     *
     * @param tenantId       Multi-tenant partition key (String — stored as VARCHAR in BaseEntity).
     * @param relationshipId BC-01 Relationship reference (opaque UUID).
     * @param workDate       Day of the attendance ledger.
     * @return Optional containing the fully hydrated AttendanceLedgerJpa if found.
     */
    @Query("""
            SELECT al FROM AttendanceLedgerJpa al
            WHERE al.tenantId = :tenantId
              AND al.relationshipId = :relationshipId
              AND al.workDate = :workDate
            """)
    Optional<AttendanceLedgerJpa> findByRelationshipAndDate(
            @Param("tenantId") String tenantId,
            @Param("relationshipId") UUID relationshipId,
            @Param("workDate") LocalDate workDate);

    /**
     * Finds a ledger by its domain UUID primary key (different from BaseEntity's Long id).
     *
     * @param ledgerId Domain UUID of the AttendanceLedger.
     * @return Optional containing the entity if found.
     */
    Optional<AttendanceLedgerJpa> findByLedgerId(UUID ledgerId);
}
