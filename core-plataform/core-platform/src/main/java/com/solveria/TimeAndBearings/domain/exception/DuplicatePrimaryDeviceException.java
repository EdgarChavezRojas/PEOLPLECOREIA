package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta registrar un segundo ClockingDevice PRIMARY
 * activo del mismo (org_unit_id, device_type), violando la Invariante de Unicidad
 * de Dispositivo Activo por OrgUnit y Tipo (BC-TM v1.2 – Aggregate 15).
 */
public class DuplicatePrimaryDeviceException extends DomainException {

    public DuplicatePrimaryDeviceException(UUID orgUnitId, String deviceType) {
        super(
                "DEVICE_PRIMARY_DUPLICATE",
                Map.of("orgUnitId", orgUnitId.toString(), "deviceType", deviceType),
                "A PRIMARY ClockingDevice of type [" + deviceType +
                "] is already ACTIVE for OrgUnit [" + orgUnitId + "] (Device Uniqueness Invariant)."
        );
    }
}
