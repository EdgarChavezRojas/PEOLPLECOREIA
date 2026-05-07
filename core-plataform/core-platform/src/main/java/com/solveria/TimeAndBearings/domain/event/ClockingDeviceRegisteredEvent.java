package com.solveria.TimeAndBearings.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio publicado cuando un {@code ClockingDevice} es registrado
 * con éxito en el sistema (WF-TM04, paso 1).
 *
 * <p>Downstream: publicado al Message Broker como {@code CLOCKING_DEVICE_REGISTERED}.
 * BC-01 Core (Audit / IT Ops) lo consume para registrar el nuevo dispositivo en el
 * inventario centralizado y disparar el proceso de sincronización de listas de empleados.
 *
 * <p><b>Payload:</b> {@code device_id}, {@code location_extension} (org_unit_id),
 * {@code capabilities} (snapshot completo del VO de capacidades del dispositivo).
 *
 * @param eventId           UUID único del evento (idempotencia).
 * @param deviceId          UUID del ClockingDevice registrado.
 * @param orgUnitId         UUID de la OrgUnit asignada (location_extension reference).
 * @param serialNumber      Número de serie del hardware.
 * @param deviceType        Tipo de dispositivo (KIOSK, BIOMETRIC_READER, etc.).
 * @param capabilities      Snapshot de las capacidades del dispositivo al momento del registro.
 * @param occurredAt        Momento en que el evento fue emitido.
 * @param tenantId          Partición multi-tenant.
 */
public record ClockingDeviceRegisteredEvent(
        UUID eventId,
        UUID deviceId,
        UUID orgUnitId,
        String serialNumber,
        String deviceType,
        DeviceCapabilities capabilities,
        Instant occurredAt,
        UUID tenantId
) implements DomainEvent {

    /** Guard clause: garantiza que el payload del evento nunca esté incompleto. */
    public ClockingDeviceRegisteredEvent {
        Objects.requireNonNull(eventId, "eventId es requerido");
        Objects.requireNonNull(deviceId, "deviceId es requerido");
        Objects.requireNonNull(orgUnitId, "orgUnitId es requerido");
        Objects.requireNonNull(serialNumber, "serialNumber es requerido");
        Objects.requireNonNull(deviceType, "deviceType es requerido");
        Objects.requireNonNull(capabilities, "capabilities es requerido");
        Objects.requireNonNull(occurredAt, "occurredAt es requerido");
        Objects.requireNonNull(tenantId, "tenantId es requerido");
    }

    /**
     * Factory que construye el evento con un nuevo {@code eventId}.
     *
     * @param deviceId     UUID del dispositivo registrado
     * @param orgUnitId    UUID de la OrgUnit asignada
     * @param serialNumber número de serie del hardware
     * @param deviceType   tipo de dispositivo como String (enum name)
     * @param capabilities capacidades del dispositivo
     * @param serverInstant instante del servidor NTP
     * @param tenantId     tenant
     * @return nuevo evento de dominio listo para publicar vía Outbox
     */
    public static ClockingDeviceRegisteredEvent of(
            UUID deviceId,
            UUID orgUnitId,
            String serialNumber,
            String deviceType,
            DeviceCapabilities capabilities,
            Instant serverInstant,
            UUID tenantId) {
        return new ClockingDeviceRegisteredEvent(
                UUID.randomUUID(),
                deviceId,
                orgUnitId,
                serialNumber,
                deviceType,
                capabilities,
                serverInstant,
                tenantId);
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
