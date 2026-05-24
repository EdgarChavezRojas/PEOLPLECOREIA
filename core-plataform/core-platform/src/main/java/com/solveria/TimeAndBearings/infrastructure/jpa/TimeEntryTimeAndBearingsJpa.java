package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.TimeAndBearings.domain.model.enums.GeoStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.TimeAndBearings.domain.model.enums.PunchType;
import com.solveria.TimeAndBearings.domain.model.vo.GeoValidationSnapshot;
import com.solveria.TimeAndBearings.domain.model.vo.PunchContext;
import com.solveria.core.shared.base.BaseEntity;
import com.solveria.core.workforce.domain.model.vo.Extension;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for {@code time_entry} table.
 *
 * <p>Append-only: never updated after initial insert (TimeEntry is immutable in the domain). All
 * columns are marked {@code updatable = false} to prevent accidental updates.
 *
 * <p>{@link PunchContext} and {@link GeoValidationSnapshot} are flattened inline — no separate join
 * table.
 */
@Entity
@Table(name = "time_entry")
public class TimeEntryTimeAndBearingsJpa extends BaseEntity {
  @Id
  @Column(name = "entry_id", nullable = false, updatable = false, columnDefinition = "UUID")
  private UUID entryId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ledger_id", nullable = false, updatable = false)
  private AttendanceLedgerJpa ledger;

  /** Assigned exclusively by the NTP server. Client timestamp is discarded (P-TM26). */
  @Column(name = "punch_time", nullable = false, updatable = false)
  private LocalDateTime punchTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "punch_type", nullable = false, updatable = false, length = 30)
  private PunchType punchType;

  // ── PunchContext (flattened) ───────────────────────────────────────────────

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false, updatable = false, length = 30)
  private PunchSource source;

  /** FK to ClockingDevice (Aggregate 15). NULL if source=MANUAL or WEB. */
  @Column(name = "device_id", columnDefinition = "UUID", updatable = false)
  private UUID deviceId;

  /** NOT NULL if source=KIOSK or BIOMETRIC_READER (Invariante Device Signature Integrity). */
  @Column(name = "device_signature", columnDefinition = "TEXT", updatable = false)
  private String deviceSignature;

  @Column(name = "ip_address", length = 45, updatable = false)
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT", updatable = false)
  private String userAgent;

  // ── GeoValidationSnapshot (flattened) [GEO-03 v1.2] ──────────────────────

  @Column(name = "latitude", precision = 9, scale = 6, updatable = false)
  private BigDecimal latitude;

  @Column(name = "longitude", precision = 9, scale = 6, updatable = false)
  private BigDecimal longitude;

  @Column(name = "accuracy_meters", precision = 8, scale = 2, updatable = false)
  private BigDecimal accuracyMeters;

  /** org_extension_snapshot [GEO-03]: snapshot of OrgUnit.extension at punch time. */
  @Enumerated(EnumType.STRING)
  @Column(name = "org_extension_snapshot", length = 3, updatable = false)
  private Extension orgExtensionSnapshot;

  @Column(name = "is_within_extension", updatable = false)
  private Boolean isWithinExtension;

  @Enumerated(EnumType.STRING)
  @Column(name = "geo_status", length = 30, updatable = false)
  private GeoStatus geoStatus;

  // ── Retroactivity & Correction (P-TM32) ──────────────────────────────────

  @Column(name = "is_retroactive", nullable = false, updatable = false)
  private boolean retroactive;

  /** NOT NULL if is_retroactive=TRUE. */
  @Column(name = "retroactive_approver_id", columnDefinition = "UUID", updatable = false)
  private UUID retroactiveApproverId;

  /** Self-reference to the original TimeEntry being corrected. */
  @Column(name = "corrects_entry_id", columnDefinition = "UUID", updatable = false)
  private UUID correctsEntryId;

  /** TRUE if anti-fraud engine detected proxy clocking (P-TM30). */
  @Column(name = "fraud_flag", nullable = false, updatable = false)
  private boolean fraudFlag;

  public TimeEntryTimeAndBearingsJpa() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public UUID getEntryId() {
    return entryId;
  }

  public void setEntryId(UUID entryId) {
    this.entryId = entryId;
  }

  public AttendanceLedgerJpa getLedger() {
    return ledger;
  }

  public void setLedger(AttendanceLedgerJpa ledger) {
    this.ledger = ledger;
  }

  public LocalDateTime getPunchTime() {
    return punchTime;
  }

  public void setPunchTime(LocalDateTime punchTime) {
    this.punchTime = punchTime;
  }

  public PunchType getPunchType() {
    return punchType;
  }

  public void setPunchType(PunchType punchType) {
    this.punchType = punchType;
  }

  public PunchSource getSource() {
    return source;
  }

  public void setSource(PunchSource source) {
    this.source = source;
  }

  public UUID getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(UUID deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceSignature() {
    return deviceSignature;
  }

  public void setDeviceSignature(String deviceSignature) {
    this.deviceSignature = deviceSignature;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public BigDecimal getAccuracyMeters() {
    return accuracyMeters;
  }

  public void setAccuracyMeters(BigDecimal accuracyMeters) {
    this.accuracyMeters = accuracyMeters;
  }

  public Extension getOrgExtensionSnapshot() {
    return orgExtensionSnapshot;
  }

  public void setOrgExtensionSnapshot(Extension orgExtensionSnapshot) {
    this.orgExtensionSnapshot = orgExtensionSnapshot;
  }

  public Boolean getIsWithinExtension() {
    return isWithinExtension;
  }

  public void setIsWithinExtension(Boolean isWithinExtension) {
    this.isWithinExtension = isWithinExtension;
  }

  public GeoStatus getGeoStatus() {
    return geoStatus;
  }

  public void setGeoStatus(GeoStatus geoStatus) {
    this.geoStatus = geoStatus;
  }

  public boolean isRetroactive() {
    return retroactive;
  }

  public void setRetroactive(boolean retroactive) {
    this.retroactive = retroactive;
  }

  public UUID getRetroactiveApproverId() {
    return retroactiveApproverId;
  }

  public void setRetroactiveApproverId(UUID retroactiveApproverId) {
    this.retroactiveApproverId = retroactiveApproverId;
  }

  public UUID getCorrectsEntryId() {
    return correctsEntryId;
  }

  public void setCorrectsEntryId(UUID correctsEntryId) {
    this.correctsEntryId = correctsEntryId;
  }

  public boolean isFraudFlag() {
    return fraudFlag;
  }

  public void setFraudFlag(boolean fraudFlag) {
    this.fraudFlag = fraudFlag;
  }
}
