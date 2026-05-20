package com.solveria.TimeAndBearings.infrastructure.adapter;

import com.solveria.TimeAndBearings.application.port.outbound.ClockingDeviceRepositoryPort;
import com.solveria.TimeAndBearings.domain.model.ar.ClockingDevice;
import com.solveria.TimeAndBearings.domain.model.entity.BiometricEnrollment;
import com.solveria.TimeAndBearings.domain.model.entity.DeviceAuditLog;
import com.solveria.TimeAndBearings.domain.model.entity.PunchAttemptLog;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceStatus;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.infrastructure.jpa.BiometricEnrollmentJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.ClockingDeviceJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.DeviceAuditLogJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.PunchAttemptLogJpa;
import com.solveria.TimeAndBearings.infrastructure.mapper.ClockingDeviceMapper;
import com.solveria.TimeAndBearings.infrastructure.repository.ClockingDeviceSpringRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Repository Adapter: ClockingDeviceRepositoryAdapter — Aggregate 15. Implements {@link
 * ClockingDeviceRepositoryPort} (outbound port).
 *
 * <p>Translates between the pure domain model and JPA entities using {@link ClockingDeviceMapper}.
 * The domain layer never depends on Spring Data or JPA directly.
 *
 * <p>Child collection strategy:
 *
 * <ul>
 *   <li>On {@code save()}: syncs enrollments, attempt logs, and audit logs delta between the domain
 *       aggregate and the JPA entity, then delegates to Spring Data.
 *   <li>On {@code findByDeviceId()}: reconstitutes the AR and loads children via {@code
 *       ClockingDevice.loadXxx()} methods.
 * </ul>
 */
@Component
public class ClockingDeviceRepositoryAdapter implements ClockingDeviceRepositoryPort {

  private final ClockingDeviceSpringRepository springRepository;
  private final ClockingDeviceMapper mapper;

  public ClockingDeviceRepositoryAdapter(
      ClockingDeviceSpringRepository springRepository, ClockingDeviceMapper mapper) {
    this.springRepository = springRepository;
    this.mapper = mapper;
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Save (create or update)
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public void save(ClockingDevice device) {
    ClockingDeviceJpa jpa =
        springRepository
            .findByDeviceIdAndTenantId(device.getDeviceId(), device.getTenantId())
            .orElseGet(
                () -> {
                  ClockingDeviceJpa newJpa = mapper.toJpa(device);
                  newJpa.setDeviceId(device.getDeviceId());
                  return newJpa;
                });

    // Update mutable state
    mapper.updateJpaFromDomain(device, jpa);

    // Sync enrollments
    syncEnrollments(device, jpa);

    // Sync punch attempt logs
    syncPunchAttemptLogs(device, jpa);

    // Sync audit logs
    syncAuditLogs(device, jpa);

    springRepository.save(jpa);
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Find
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public Optional<ClockingDevice> findByDeviceId(UUID deviceId, UUID tenantId) {
    return springRepository
        .findByDeviceIdAndTenantId(deviceId, tenantId)
        .map(this::reconstitute);
  }

  @Override
  public boolean existsActivePrimaryDevice(UUID orgUnitId, DeviceType deviceType, UUID tenantId) {
    return springRepository.existsActivePrimaryByOrgUnitAndType(
        orgUnitId, deviceType, DeviceStatus.ACTIVE, tenantId);
  }

  @Override
  public List<ClockingDevice> findActiveByOrgUnit(UUID orgUnitId, UUID tenantId) {
    return springRepository
        .findByOrgUnitIdAndStatusAndTenantId(orgUnitId, DeviceStatus.ACTIVE, tenantId)
        .stream()
        .map(this::reconstitute)
        .toList();
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Reconstitution
  // ─────────────────────────────────────────────────────────────────────────

  private ClockingDevice reconstitute(ClockingDeviceJpa jpa) {
    ClockingDevice domain = mapper.toDomain(jpa);

    List<BiometricEnrollment> enrollments = mapper.toEnrollmentDomainList(jpa.getEnrollments());
    domain.loadEnrollments(enrollments);

    List<PunchAttemptLog> logs = mapper.toAttemptLogDomainList(jpa.getPunchAttemptLogs());
    domain.loadPunchAttemptLogs(logs);

    List<DeviceAuditLog> auditLogs = mapper.toAuditLogDomainList(jpa.getAuditLogs());
    domain.loadAuditLogs(auditLogs);

    return domain;
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  Child collection sync helpers
  // ─────────────────────────────────────────────────────────────────────────

  private void syncEnrollments(ClockingDevice device, ClockingDeviceJpa jpa) {
    // Add new enrollments not yet persisted
    for (BiometricEnrollment domainEnrollment : device.getEnrollments()) {
      boolean alreadyPersisted =
          jpa.getEnrollments().stream()
              .anyMatch(e -> e.getEnrollmentId().equals(domainEnrollment.getEnrollmentId()));

      if (!alreadyPersisted) {
        BiometricEnrollmentJpa enrollmentJpa = mapper.toJpa(domainEnrollment);
        enrollmentJpa.setDevice(jpa);
        enrollmentJpa.setEnrollmentId(domainEnrollment.getEnrollmentId());
        jpa.getEnrollments().add(enrollmentJpa);
      } else {
        // Update existing enrollment status (e.g., revocation)
        jpa.getEnrollments().stream()
            .filter(e -> e.getEnrollmentId().equals(domainEnrollment.getEnrollmentId()))
            .findFirst()
            .ifPresent(
                e -> {
                  e.setStatus(domainEnrollment.getStatus());
                  e.setRevokedAt(domainEnrollment.getRevokedAt());
                  e.setRevocationReason(domainEnrollment.getRevocationReason());
                });
      }
    }
  }

  private void syncPunchAttemptLogs(ClockingDevice device, ClockingDeviceJpa jpa) {
    for (PunchAttemptLog domainLog : device.getPunchAttemptLogs()) {
      boolean alreadyPersisted =
          jpa.getPunchAttemptLogs().stream()
              .anyMatch(l -> l.getAttemptId().equals(domainLog.getAttemptId()));

      if (!alreadyPersisted) {
        PunchAttemptLogJpa logJpa = mapper.toJpa(domainLog);
        logJpa.setDevice(jpa);
        logJpa.setAttemptId(domainLog.getAttemptId());
        jpa.getPunchAttemptLogs().add(logJpa);
      } else {
        // Update escalation if assigned async
        jpa.getPunchAttemptLogs().stream()
            .filter(l -> l.getAttemptId().equals(domainLog.getAttemptId()))
            .findFirst()
            .ifPresent(l -> l.setIncidentEscalatedTo(domainLog.getIncidentEscalatedTo()));
      }
    }
  }

  private void syncAuditLogs(ClockingDevice device, ClockingDeviceJpa jpa) {
    for (DeviceAuditLog domainAudit : device.getAuditLogs()) {
      boolean alreadyPersisted =
          jpa.getAuditLogs().stream()
              .anyMatch(a -> a.getAuditLogId().equals(domainAudit.getAuditLogId()));

      if (!alreadyPersisted) {
        DeviceAuditLogJpa auditJpa = mapper.toJpa(domainAudit);
        auditJpa.setDevice(jpa);
        auditJpa.setAuditLogId(domainAudit.getAuditLogId());
        jpa.getAuditLogs().add(auditJpa);
      }
      // Audit logs are immutable — no update path
    }
  }
}
