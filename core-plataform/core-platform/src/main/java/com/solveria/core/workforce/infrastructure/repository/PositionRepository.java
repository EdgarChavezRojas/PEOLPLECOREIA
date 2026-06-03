package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.domain.model.vo.PositionStatus;
import com.solveria.core.workforce.infrastructure.jpa.PositionJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<PositionJpa, UUID> {

  @Query(
      value =
          """
            select p.*
            from position p
            join org_unit ou on ou.unit_id = p.unit_id
            where p.position_id = :positionId
              and ou.tenant_id = :tenantId
            """,
      nativeQuery = true)
  Optional<PositionJpa> findByPositionIdAndTenantId(
      @Param("positionId") UUID positionId, @Param("tenantId") UUID tenantId);

  @Query(
      value =
          """
            select count(*)
            from position p
            join org_unit ou on ou.unit_id = p.unit_id
            where p.job_id = :jobId
              and ou.tenant_id = :tenantId
            """,
      nativeQuery = true)
  int countByJobIdAndTenantId(@Param("jobId") UUID jobId, @Param("tenantId") UUID tenantId);

  @Query(
      value =
          """
            select p.*
            from position p
            join org_unit ou on ou.unit_id = p.unit_id
            where ou.tenant_id = :tenantId
            """,
      nativeQuery = true)
  List<PositionJpa> findByTenantId(@Param("tenantId") UUID tenantId);

  @Query(
      value =
          """
            select p.*
            from position p
            join org_unit ou on ou.unit_id = p.unit_id
            where ou.tenant_id = :tenantId
              and p.pos_status = :status
            """,
      nativeQuery = true)
  List<PositionJpa> findByTenantIdAndStatus(
      @Param("tenantId") UUID tenantId, @Param("status") PositionStatus status);

  @Query(
      value =
          """
            select count(*) > 0
            from position p
            join org_unit ou on ou.unit_id = p.unit_id
            where p.unit_id = :unitId
              and p.job_id = :jobId
              and ou.tenant_id = :tenantId
            """,
      nativeQuery = true)
  boolean existsByUnitIdAndJobIdAndTenantId(
      @Param("unitId") UUID unitId, @Param("jobId") UUID jobId, @Param("tenantId") UUID tenantId);
}
