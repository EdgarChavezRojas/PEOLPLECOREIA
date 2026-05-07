package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Resultado del chequeo geográfico departamental (Extension-Based Geo-Fencing).
 * Definido en GeoValidationSnapshot.geo_status – BC-TM v1.2 [GEO-03 ACTUALIZADO].
 *
 * <p>La geocerca NO es un polígono ni un radio almacenado en BC-TM.
 * BC-TM consulta el {@code org_extension} (ENUM departamental) de la {@code OrgUnit}
 * en BC-01 Core y guarda una snapshot del resultado (P-TM28).
 */
public enum GeoStatus {

    /** Las coordenadas GPS corresponden al departamento org_extension_snapshot. is_within_extension=TRUE. */
    INSIDE,

    /**
     * Las coordenadas GPS no corresponden al departamento org_extension_snapshot.
     * TimeEntry persiste con este estado; excepción GEO_VIOLATION se genera de forma asíncrona.
     * El dispositivo permanece operativo (Non-Blocking Design, P-TM28).
     */
    OUTSIDE_FENCE,

    /**
     * Jornada remota con RemoteWorkAuth activo (COMISION_SERVICIO / TELETRABAJO).
     * La restricción de org_extension no aplica; coordenadas se registran para trazabilidad.
     */
    REMOTE_AUTHORIZED,

    /**
     * Canal KIOSK o WEB: no se dispone de coordenadas GPS.
     * latitude/longitude = NULL en GeoValidationSnapshot.
     */
    NO_GPS
}
