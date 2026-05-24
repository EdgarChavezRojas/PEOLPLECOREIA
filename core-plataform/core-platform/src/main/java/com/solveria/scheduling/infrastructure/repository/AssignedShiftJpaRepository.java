package com.solveria.scheduling.infrastructure.repository;

import com.solveria.scheduling.infrastructure.jpa.AssignedShiftJpa;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para realizar consultas directas y eficientes sobre la tabla 'sch_assigned_shift'.
 */
@Repository
public interface AssignedShiftJpaRepository extends JpaRepository<AssignedShiftJpa, Long> {

  /**
   * Obtiene los turnos asignados de un empleado en un rango de fecha y hora específicos,
   * filtrado por tenant y asegurando que estén activos.
   *
   * @param relationshipId id del empleado/relación.
   * @param tenantId id del inquilino para aislamiento multi-tenant.
   * @param startDateTime fecha y hora de inicio de búsqueda.
   * @param endDateTime fecha y hora de fin de búsqueda.
   * @return Lista de turnos asignados recuperados.
   */
  @Query(
      "SELECT a FROM AssignedShiftJpa a WHERE a.relationshipId = :relationshipId "
          + "AND a.tenantId = :tenantId "
          + "AND a.expectedStart >= :startDateTime AND a.expectedEnd <= :endDateTime "
          + "AND a.isActive = true")
  List<AssignedShiftJpa> findByRelationshipIdAndDateRange(
      @Param("relationshipId") UUID relationshipId,
      @Param("tenantId") UUID tenantId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime);
}
