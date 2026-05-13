package com.solveria.TimeAndBearings.infrastructure.repository;

import com.solveria.TimeAndBearings.domain.model.enums.DeviceStatus;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.infrastructure.adapter.ClockingDeviceRepositoryAdapter;
import com.solveria.TimeAndBearings.infrastructure.jpa.ClockingDeviceJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for {@link ClockingDeviceJpa} — Aggregate 15.
 * Used exclusively by {@link ClockingDeviceRepositoryAdapter}.
 */
public interface ClockingDeviceSpringRepository extends JpaRepository<ClockingDeviceJpa, UUID> {

    Optional<ClockingDeviceJpa> findByDeviceIdAndTenantId(UUID deviceId, String tenantId);

    /**
     * Verifies Device Uniqueness Invariant: checks for an existing ACTIVE PRIMARY device
     * of the same type in the same OrgUnit.
     */
    @Query("""
            SELECT COUNT(d) > 0
            FROM ClockingDeviceJpa d
            WHERE d.orgUnitId = :orgUnitId
              AND d.deviceType = :deviceType
              AND d.deviceRole = com.solveria.TimeAndBearings.domain.model.enums.DeviceRole.PRIMARY
              AND d.status = :activeStatus
              AND d.tenantId = :tenantId
            """)
    boolean existsActivePrimaryByOrgUnitAndType(
            @Param("orgUnitId") UUID orgUnitId,
            @Param("deviceType") DeviceType deviceType,
            @Param("activeStatus") DeviceStatus activeStatus,
            @Param("tenantId") String tenantId
    );

    /**
     * Finds all ACTIVE devices in a given OrgUnit for employee sync and bulk revocation.
     */
    List<ClockingDeviceJpa> findByOrgUnitIdAndStatusAndTenantId(
            UUID orgUnitId,
            DeviceStatus status,
            String tenantId
    );
}
