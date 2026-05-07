package com.solveria.TimeAndBearings.application.port.outbound;

import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.infrastructure.adapter.ClockingDeviceRepositoryAdapter;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port: ClockingDeviceRepositoryPort.
 * Define el contrato de persistencia para el Aggregate 15 (ClockingDevice).
 *
 * <p>Implementado por {@link ClockingDeviceRepositoryAdapter}.
 * El dominio NO conoce JPA ni Spring Data — solo este puerto.
 */
public interface ClockingDeviceRepositoryPort {

    /**
     * Persiste un ClockingDevice nuevo o actualizado.
     * Incluye sus enrollments, attempt logs y audit logs.
     */
    void save(ClockingDevice device);

    /**
     * Encuentra un ClockingDevice por su UUID (dentro del tenant).
     */
    Optional<ClockingDevice> findByDeviceId(UUID deviceId, UUID tenantId);

    /**
     * Verifica si ya existe un ClockingDevice PRIMARY ACTIVO del mismo tipo en la OrgUnit.
     * Usado para enforcer la Invariante de Unicidad antes de registrar un nuevo dispositivo.
     *
     * @return true si ya existe un PRIMARY ACTIVE para ese (orgUnitId, deviceType).
     */
    boolean existsActivePrimaryDevice(UUID orgUnitId, DeviceType deviceType, UUID tenantId);

    /**
     * Encuentra todos los ClockingDevice activos de una OrgUnit.
     * Usado durante la sincronización de empleados (WF-TM04 paso 3).
     */
    java.util.List<ClockingDevice> findActiveByOrgUnit(UUID orgUnitId, UUID tenantId);
}
