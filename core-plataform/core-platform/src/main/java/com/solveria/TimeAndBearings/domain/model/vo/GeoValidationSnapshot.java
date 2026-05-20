package com.solveria.TimeAndBearings.domain.model.vo;

import com.solveria.TimeAndBearings.domain.model.enums.GeoStatus;
import com.solveria.core.workforce.domain.model.vo.Extension;
import java.math.BigDecimal;

/**
 * Resultado del cruce entre las coordenadas GPS del dispositivo y el departamento (Extension ENUM)
 * configurado en la OrgUnit de BC-01 Core. Value Object – Aggregate 14: AttendanceLedger [GEO-02/03
 * ACTUALIZADO v1.2].
 *
 * <p>La geocerca NO es un polígono almacenado en BC-TM. BC-TM consulta el {@code org_extension} de
 * BC-01 Core y captura una snapshot aquí para trazabilidad histórica, incluso si Core actualiza el
 * registro posterior a la marcación (P-TM28 / Regla ACL GeoExtension).
 *
 * @param latitude Coordenada GPS. NULL si source=KIOSK/WEB.
 * @param longitude Coordenada GPS. NULL si source=KIOSK/WEB.
 * @param accuracyMeters Precisión GPS reportada por el dispositivo.
 * @param orgExtensionSnapshot Copia del Extension de la OrgUnit en el momento de la marcación
 *     [GEO-03].
 * @param isWithinExtension TRUE si coordenadas corresponden al departamento. NOT NULL para canal
 *     MOBILE.
 * @param geoStatus Resultado del chequeo (INSIDE, OUTSIDE_FENCE, REMOTE_AUTHORIZED, NO_GPS).
 */
public record GeoValidationSnapshot(
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal accuracyMeters,
    Extension orgExtensionSnapshot,
    Boolean isWithinExtension,
    GeoStatus geoStatus) {

  /** Factory para canales sin GPS (KIOSK / WEB). */
  public static GeoValidationSnapshot noGps() {
    return new GeoValidationSnapshot(null, null, null, null, null, GeoStatus.NO_GPS);
  }

  /**
   * Factory para jornada remota autorizada (COMISION_SERVICIO / CONTINGENCIA). Las coordenadas se
   * registran para trazabilidad pero no se valida el departamento (P-TM28).
   */
  public static GeoValidationSnapshot remoteAuthorized(
      BigDecimal latitude,
      BigDecimal longitude,
      BigDecimal accuracyMeters,
      Extension orgExtensionSnapshot) {
    return new GeoValidationSnapshot(
        latitude,
        longitude,
        accuracyMeters,
        orgExtensionSnapshot,
        null,
        GeoStatus.REMOTE_AUTHORIZED);
  }
}
