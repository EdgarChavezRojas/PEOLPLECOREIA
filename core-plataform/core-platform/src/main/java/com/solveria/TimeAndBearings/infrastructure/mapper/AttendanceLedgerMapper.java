package com.solveria.TimeAndBearings.infrastructure.mapper;

import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.entity.TimeDeviationRecord;
import com.solveria.TimeAndBearings.domain.model.entity.TimeEntry;
import com.solveria.TimeAndBearings.domain.model.enums.GeoStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.TimeAndBearings.domain.model.vo.GeoValidationSnapshot;
import com.solveria.TimeAndBearings.domain.model.vo.PunchContext;
import com.solveria.TimeAndBearings.domain.model.vo.WorkedHoursSummary;
import com.solveria.TimeAndBearings.infrastructure.jpa.AttendanceLedgerJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.TimeDeviationRecordJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.TimeEntryTimeAndBearingsJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.WorkedHoursSummaryEmbeddable;
import java.util.UUID;
import org.mapstruct.*;

/**
 * MapStruct mapper: domain model ↔ JPA persistence model for Aggregate 14.
 *
 * <p>This mapper is the Anti-Corruption boundary between the persistence layer and the domain. No
 * Spring/JPA annotations ever leak into domain classes.
 *
 * <p>Mapping directions:
 *
 * <ul>
 *   <li><b>Domain → JPA</b> ({@code toJpa} methods): used by the adapter before saving.
 *   <li><b>JPA → Domain</b> ({@code toDomain} methods): used by the adapter on reconstitution.
 * </ul>
 *
 * <p>{@link PunchContext} and {@link GeoValidationSnapshot} are Java Records (VOs) that are stored
 * as flat columns in {@link TimeEntryTimeAndBearingsJpa}. The helper methods {@link
 * #buildPunchContext(TimeEntryTimeAndBearingsJpa)} and {@link
 * #buildGeoSnapshot(TimeEntryTimeAndBearingsJpa)} reconstruct them during mapping — they are also
 * called directly by the adapter for individual TimeEntry reconstitution.
 */
@Mapper(componentModel = "spring")
public interface AttendanceLedgerMapper {

  // ─── AttendanceLedger ────────────────────────────────────────────────────

  @Mapping(target = "tenantId", source = "tenantId", qualifiedByName = "uuidToString")
  @Mapping(target = "orgUnitId", source = "orgUnitId")
  @Mapping(target = "workedHoursSummary", ignore = true) // set manually by adapter
  @Mapping(target = "timeEntries", ignore = true) // children managed by adapter
  @Mapping(target = "deviations", ignore = true)
  AttendanceLedgerJpa toJpa(AttendanceLedger domain);

  // ─── TimeEntry ────────────────────────────────────────────────────────────

  @Mapping(target = "ledger", ignore = true) // set by adapter after root lookup
  @Mapping(target = "source", source = "punchContext.sourceChannel")
  @Mapping(target = "deviceId", source = "punchContext.deviceId")
  @Mapping(target = "ipAddress", source = "punchContext.ipAddress")
  @Mapping(target = "userAgent", source = "punchContext.userAgent")
  @Mapping(target = "latitude", source = "geoSnapshot.latitude")
  @Mapping(target = "longitude", source = "geoSnapshot.longitude")
  @Mapping(target = "accuracyMeters", source = "geoSnapshot.accuracyMeters")
  @Mapping(target = "orgExtensionSnapshot", source = "geoSnapshot.orgExtensionSnapshot")
  @Mapping(target = "isWithinExtension", source = "geoSnapshot.isWithinExtension")
  @Mapping(target = "geoStatus", source = "geoSnapshot.geoStatus")
  @Mapping(target = "retroactive", source = "retroactive")
  TimeEntryTimeAndBearingsJpa toJpa(TimeEntry domain);

  // ─── TimeDeviationRecord ─────────────────────────────────────────────────

  @Mapping(target = "ledger", ignore = true)
  @Mapping(target = "tenantId", ignore = true)
  TimeDeviationRecordJpa toJpa(TimeDeviationRecord domain);

  @Mapping(target = "ledgerId", ignore = true)
  @Mapping(target = "auditTrail", ignore = true)
  TimeDeviationRecord toDomain(TimeDeviationRecordJpa jpa);

  // ─── WorkedHoursSummary ──────────────────────────────────────────────────

  WorkedHoursSummaryEmbeddable toEmbeddable(WorkedHoursSummary domain);

  WorkedHoursSummary toDomain(WorkedHoursSummaryEmbeddable embeddable);

  // ─── Named qualifiers ─────────────────────────────────────────────────────

  @Named("uuidToString")
  default String uuidToString(UUID uuid) {
    return uuid != null ? uuid.toString() : null;
  }

  @Named("stringToUuid")
  default UUID stringToUuid(String str) {
    return str != null ? UUID.fromString(str) : null;
  }

  // ─── VO reconstruction helpers (called by adapter during reconstitution) ──

  /**
   * Reconstructs the {@link PunchContext} record from flat JPA columns. Falls back to MANUAL source
   * if the stored source causes a validation failure (defensive guard for legacy data).
   */
  default PunchContext buildPunchContext(TimeEntryTimeAndBearingsJpa jpa) {
    if (jpa.getSource() == null) return null;
    try {
      return new PunchContext(
          jpa.getDeviceId(), jpa.getSource(), jpa.getIpAddress(), jpa.getUserAgent());
    } catch (IllegalArgumentException ex) {
      // Defensive: return a minimal context that won't throw, used only for read-side
      return new PunchContext(
          jpa.getDeviceId(), PunchSource.MANUAL, jpa.getIpAddress(), jpa.getUserAgent());
    }
  }

  /** Reconstructs the {@link GeoValidationSnapshot} record from flat JPA columns. */
  default GeoValidationSnapshot buildGeoSnapshot(TimeEntryTimeAndBearingsJpa jpa) {
    GeoStatus status = jpa.getGeoStatus() != null ? jpa.getGeoStatus() : GeoStatus.NO_GPS;
    if (status == GeoStatus.NO_GPS) {
      return GeoValidationSnapshot.noGps();
    }
    if (status == GeoStatus.REMOTE_AUTHORIZED) {
      return GeoValidationSnapshot.remoteAuthorized(
          jpa.getLatitude(), jpa.getLongitude(),
          jpa.getAccuracyMeters(), jpa.getOrgExtensionSnapshot());
    }
    return new GeoValidationSnapshot(
        jpa.getLatitude(),
        jpa.getLongitude(),
        jpa.getAccuracyMeters(),
        jpa.getOrgExtensionSnapshot(),
        jpa.getIsWithinExtension(),
        status);
  }
}
