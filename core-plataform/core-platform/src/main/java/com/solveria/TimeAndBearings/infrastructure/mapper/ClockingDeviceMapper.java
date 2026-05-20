package com.solveria.TimeAndBearings.infrastructure.mapper;

import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.entity.BiometricEnrollment;
import com.solveria.TimeAndBearings.domain.model.entity.DeviceAuditLog;
import com.solveria.TimeAndBearings.domain.model.entity.PunchAttemptLog;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceHeartbeat;
import com.solveria.TimeAndBearings.infrastructure.jpa.*;
import java.util.List;
import org.mapstruct.*;

/**
 * MapStruct Mapper: ClockingDeviceMapper — Aggregate 15. Translates between the pure domain model
 * and JPA entities.
 *
 * <p>Uses {@code componentModel = "spring"} to produce a Spring bean injectable anywhere in the
 * infrastructure layer. The domain layer NEVER depends on this mapper.
 *
 * <p>All {@code @Mapping} annotations with {@code ignore = true} cover fields managed by {@link
 * com.solveria.core.shared.base.BaseEntity} (id, version, createdAt, createdBy, etc.) to avoid
 * overwriting JPA-managed audit columns.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClockingDeviceMapper {

  // ── ClockingDevice AR ↔ JPA ───────────────────────────────────────────────

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "tenantId", source = "tenantId")
  @Mapping(target = "capabilities", source = "capabilities")
  @Mapping(target = "heartbeat", source = "heartbeat")
  @Mapping(target = "enrollments", ignore = true) // managed separately
  @Mapping(target = "punchAttemptLogs", ignore = true)
  @Mapping(target = "auditLogs", ignore = true)
  ClockingDeviceJpa toJpa(ClockingDevice domain);

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "tenantId", source = "tenantId")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "decommissionedAt", source = "decommissionedAt")
  @Mapping(target = "capabilities", source = "capabilities")
  @Mapping(target = "heartbeat", source = "heartbeat")
  @Mapping(target = "enrollments", ignore = true)
  @Mapping(target = "punchAttemptLogs", ignore = true)
  @Mapping(target = "auditLogs", ignore = true)
  void updateJpaFromDomain(ClockingDevice domain, @MappingTarget ClockingDeviceJpa jpa);

  /** Maps JPA → domain AR (reconstitution). Children loaded separately via loadXxx(). */
  // @Mapping(target = "tenantId", expression = "jpa.getTenantId()")
  @Mapping(target = "domainEvents", ignore = true)
  ClockingDevice toDomain(ClockingDeviceJpa jpa);

  // ── DeviceCapabilities VO ↔ Embeddable ────────────────────────────────────

  DeviceCapabilitiesEmbeddable toEmbeddable(DeviceCapabilities vo);

  @Mapping(target = "supportsFingerprint", source = "supportsFingerprint")
  @Mapping(target = "supportsFacial", source = "supportsFacial")
  @Mapping(target = "supportsNfc", source = "supportsNfc")
  @Mapping(target = "supportsQr", source = "supportsQr")
  DeviceCapabilities toCapabilitiesVo(DeviceCapabilitiesEmbeddable embeddable);

  // ── DeviceHeartbeat VO ↔ Embeddable ──────────────────────────────────────

  DeviceHeartbeatEmbeddable toEmbeddable(DeviceHeartbeat vo);

  DeviceHeartbeat toHeartbeatVo(DeviceHeartbeatEmbeddable embeddable);

  // ── BiometricEnrollment Entity ↔ JPA ─────────────────────────────────────

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "tenantId", ignore = true)
  @Mapping(target = "device", ignore = true) // set by adapter
  BiometricEnrollmentJpa toJpa(BiometricEnrollment domain);

  List<BiometricEnrollment> toEnrollmentDomainList(List<BiometricEnrollmentJpa> jpaList);

  // ── PunchAttemptLog Entity ↔ JPA ─────────────────────────────────────────

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "tenantId", ignore = true)
  @Mapping(target = "device", ignore = true)
  PunchAttemptLogJpa toJpa(PunchAttemptLog domain);

  List<PunchAttemptLog> toAttemptLogDomainList(List<PunchAttemptLogJpa> jpaList);

  // ── DeviceAuditLog Entity ↔ JPA ──────────────────────────────────────────

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  @Mapping(target = "lastModifiedBy", ignore = true)
  @Mapping(target = "tenantId", ignore = true)
  @Mapping(target = "device", ignore = true)
  DeviceAuditLogJpa toJpa(DeviceAuditLog domain);

  List<DeviceAuditLog> toAuditLogDomainList(List<DeviceAuditLogJpa> jpaList);

  // ── BiometricEnrollment Entity ↔ JPA ─────────────────────────────────────

  default BiometricEnrollment toDomain(BiometricEnrollmentJpa jpa) {
    if (jpa == null) return null;
    return new BiometricEnrollment(
        jpa.getEnrollmentId(),
        jpa.getDevice() != null ? jpa.getDevice().getDeviceId() : null,
        jpa.getRelationshipId(),
        jpa.getBiometricType(),
        jpa.getTemplateHash(),
        jpa.getTemplateQualityScore(),
        jpa.getStatus(),
        jpa.getEnrolledAt(),
        jpa.getRevokedAt(),
        jpa.getRevocationReason());
  }

  // ── PunchAttemptLog Entity ↔ JPA ─────────────────────────────────────────

  default PunchAttemptLog toDomain(PunchAttemptLogJpa jpa) {
    if (jpa == null) return null;
    return new PunchAttemptLog(
        jpa.getAttemptId(),
        jpa.getDevice() != null ? jpa.getDevice().getDeviceId() : null,
        jpa.getAttemptedAt(),
        jpa.getRelationshipId(),
        jpa.getAuthMethod(),
        jpa.getAuthResult(),
        jpa.isSecurityIncident(),
        jpa.getIncidentEscalatedTo());
  }

  // ── DeviceAuditLog Entity ↔ JPA ──────────────────────────────────────────

  default DeviceAuditLog toDomain(DeviceAuditLogJpa jpa) {
    if (jpa == null) return null;
    return new DeviceAuditLog(
        jpa.getAuditLogId(),
        jpa.getDevice() != null ? jpa.getDevice().getDeviceId() : null,
        jpa.getEventType(),
        jpa.getActorId(),
        jpa.getOccurredAt(),
        jpa.getDescription());
  }
}
