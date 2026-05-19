package com.solveria.TimeAndBearings.domain.model.ar;

import com.solveria.TimeAndBearings.application.port.outbound.ClockingDeviceRepositoryPort;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.TimeAndBearings.domain.event.BiometricEnrollmentRevokedEvent;
import com.solveria.TimeAndBearings.domain.event.ClockingDeviceRegisteredEvent;
import com.solveria.TimeAndBearings.domain.event.SecurityPunchIncidentEvent;
import com.solveria.TimeAndBearings.domain.exception.DuplicateBiometricEnrollmentException;
import com.solveria.TimeAndBearings.domain.exception.InvalidDeviceSignatureException;
import com.solveria.TimeAndBearings.domain.model.entity.BiometricEnrollment;
import com.solveria.TimeAndBearings.domain.model.entity.DeviceAuditLog;
import com.solveria.TimeAndBearings.domain.model.entity.PunchAttemptLog;
import com.solveria.TimeAndBearings.domain.model.enums.AuthMethod;
import com.solveria.TimeAndBearings.domain.model.enums.AuthResult;
import com.solveria.TimeAndBearings.domain.model.enums.BiometricType;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceRole;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceStatus;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import com.solveria.TimeAndBearings.domain.model.enums.RevocationReason;
import com.solveria.TimeAndBearings.domain.model.enums.SyncStatus;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import com.solveria.TimeAndBearings.domain.model.vo.DeviceHeartbeat;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root 15: ClockingDevice (Registro de Dispositivos).
 *
 * <p>Representa un dispositivo físico o canal lógico (MOBILE_APP_CHANNEL) autorizado
 * para recibir marcaciones. Tiene identidad criptográfica única y gestiona el ciclo
 * de vida de las credenciales biométricas de los colaboradores.
 *
 * <h3>Invariantes Enforced</h3>
 * <ol>
 *   <li><b>Device Uniqueness:</b> Por cada (org_unit_id, device_type) solo puede existir
 *       UN ClockingDevice con role=PRIMARY en status=ACTIVE. Verificado en la capa de
 *       aplicación antes de invocar el factory.</li>
 *   <li><b>Device Signature Integrity:</b> Todo TimeEntry originado desde KIOSK o
 *       BIOMETRIC_READER debe incluir una firma digital válida. {@link #validateSignature}
 *       lanza {@link InvalidDeviceSignatureException} si la firma es inválida o ausente.</li>
 *   <li><b>Biometric Enrollment Uniqueness:</b> Un collaborador puede tener máximo un
 *       template ACTIVE por BiometricType en este dispositivo. {@link #enrollBiometric}
 *       lanza {@link DuplicateBiometricEnrollmentException} si ya existe uno activo.</li>
 * </ol>
 *
 * <h3>Non-Blocking Design (WF-TM04)</h3>
 * Todo intento de autenticación genera un {@link PunchAttemptLog}. Si se detecta fraude
 * o credenciales revocadas, se registra {@code security_incident=TRUE} y se emite
 * {@link SecurityPunchIncidentEvent}. El dispositivo continúa operativo al 100%.
 *
 * <p>Puro Java 21 — ninguna anotación Spring/JPA.
 */
public class ClockingDevice {

    private UUID deviceId;
    private UUID tenantId;
    private UUID orgUnitId;
    private String serialNumber;
    private DeviceType deviceType;
    private DeviceRole deviceRole;
    private DeviceStatus status;
    private LocalDateTime installedAt;
    private LocalDateTime decommissionedAt;
    private DeviceCapabilities capabilities;
    private DeviceHeartbeat heartbeat;
    private List<BiometricEnrollment> enrollments;
    private List<PunchAttemptLog> punchAttemptLogs;
    private List<DeviceAuditLog> auditLogs;
    private List<DomainEvent> domainEvents;

    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public void setOrgUnitId(UUID orgUnitId) { this.orgUnitId = orgUnitId; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }
    public void setDeviceRole(DeviceRole deviceRole) { this.deviceRole = deviceRole; }
    public void setStatus(DeviceStatus status) { this.status = status; }
    public void setInstalledAt(LocalDateTime installedAt) { this.installedAt = installedAt; }
    public void setDecommissionedAt(LocalDateTime decommissionedAt) { this.decommissionedAt = decommissionedAt; }
    public void setCapabilities(DeviceCapabilities capabilities) { this.capabilities = capabilities; }
    public void setHeartbeat(DeviceHeartbeat heartbeat) { this.heartbeat = heartbeat; }
    public void setEnrollments(List<BiometricEnrollment> enrollments) { this.enrollments = enrollments; }
    public void setPunchAttemptLogs(List<PunchAttemptLog> punchAttemptLogs) { this.punchAttemptLogs = punchAttemptLogs; }
    public void setAuditLogs(List<DeviceAuditLog> auditLogs) { this.auditLogs = auditLogs; }
    public void setDomainEvents(List<DomainEvent> domainEvents) { this.domainEvents = domainEvents; }
    /**
     * Factory: Registra un nuevo ClockingDevice en estado PROVISIONING (WF-TM04 paso 1).
     * La Invariante de Unicidad PRIMARY se verifica en la capa de aplicación ANTES de llamar
     * a este factory (ver {@link ClockingDeviceRepositoryPort}).
     *
     * @param tenantId       Tenant del dispositivo.
     * @param orgUnitId      OrgUnit a la que se asigna el dispositivo (BC-01 Core ref opaca).
     * @param serialNumber   Número de serie único del hardware dentro del tenant.
     * @param deviceType     Tipo de dispositivo.
     * @param deviceRole     Rol dentro de la OrgUnit.
     * @param capabilities   Capacidades físicas/lógicas del dispositivo.
     * @param registeredAt   Hora del servidor NTP del registro.
     * @param actorId        ID del administrador de TI que registra el dispositivo.
     * @return Nuevo ClockingDevice en estado PROVISIONING.
     */
    public static ClockingDevice register(
            UUID tenantId,
            UUID orgUnitId,
            String serialNumber,
            DeviceType deviceType,
            DeviceRole deviceRole,
            DeviceCapabilities capabilities,
            LocalDateTime registeredAt,
            String actorId) {

        ClockingDevice device = new ClockingDevice(
                UUID.randomUUID(), tenantId, orgUnitId, serialNumber,
                deviceType, deviceRole, DeviceStatus.PROVISIONING,
                registeredAt, null, capabilities, null);

        device.auditLogs.add(new DeviceAuditLog(
                UUID.randomUUID(), device.deviceId, "PROVISIONED",
                actorId, registeredAt,
                "Device registered. SerialNumber=" + serialNumber +
                " Type=" + deviceType + " Role=" + deviceRole));

        // Emit ClockingDeviceRegisteredEvent for downstream consumers (BC-01 IT Ops / Audit)
        device.domainEvents.add(ClockingDeviceRegisteredEvent.of(
                device.deviceId,
                orgUnitId,
                serialNumber,
                deviceType.name(),
                capabilities,
                registeredAt.toInstant(ZoneOffset.UTC),
                tenantId));

        return device;
    }

    /** Reconstitution constructor (used by repository adapter). */
    public ClockingDevice(
            UUID deviceId,
            UUID tenantId,
            UUID orgUnitId,
            String serialNumber,
            DeviceType deviceType,
            DeviceRole deviceRole,
            DeviceStatus status,
            LocalDateTime installedAt,
            LocalDateTime decommissionedAt,
            DeviceCapabilities capabilities,
            DeviceHeartbeat heartbeat) {

        this.deviceId = deviceId;
        this.tenantId = tenantId;
        this.orgUnitId = orgUnitId;
        this.serialNumber = serialNumber;
        this.deviceType = deviceType;
        this.deviceRole = deviceRole;
        this.status = status;
        this.installedAt = installedAt;
        this.decommissionedAt = decommissionedAt;
        this.capabilities = capabilities;
        this.heartbeat = heartbeat;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WF-TM04 — Device Lifecycle Management
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Activa el dispositivo después de que el par de claves fue instalado (WF-TM04 paso 2).
     * Actualiza las capacidades con la clave pública definitiva y emite el heartbeat inicial.
     *
     * @param activatedCapabilities Capacidades actualizadas (incluye publicKeyPem para KIOSK/BIOMETRIC_READER).
     * @param activatedAt           Hora del servidor NTP.
     * @param actorId               Administrador de TI.
     */
    public void activate(DeviceCapabilities activatedCapabilities, LocalDateTime activatedAt, String actorId) {
        if (this.status != DeviceStatus.PROVISIONING) {
            throw new IllegalStateException(
                    "ClockingDevice [" + deviceId + "] can only be activated from PROVISIONING. Current: " + status);
        }
        this.capabilities = activatedCapabilities;
        this.status = DeviceStatus.ACTIVE;
        this.heartbeat = DeviceHeartbeat.initial(activatedAt);

        auditLogs.add(new DeviceAuditLog(
                UUID.randomUUID(), deviceId, "ACTIVATED",
                actorId, activatedAt, "Device activated. PublicKey installed."));
    }

    /**
     * Suspende el dispositivo (ej. pérdida, mantenimiento). Non-Blocking: los demás
     * dispositivos de la OrgUnit continúan operativos.
     *
     * @param suspendedAt Hora del servidor NTP.
     * @param actorId     Actor que suspende.
     * @param reason      Razón de la suspensión.
     */
    public void suspend(LocalDateTime suspendedAt, String actorId, String reason) {
        if (this.status == DeviceStatus.DECOMMISSIONED) {
            throw new IllegalStateException(
                    "ClockingDevice [" + deviceId + "] is already DECOMMISSIONED.");
        }
        this.status = DeviceStatus.SUSPENDED;
        auditLogs.add(new DeviceAuditLog(
                UUID.randomUUID(), deviceId, "SUSPENDED",
                actorId, suspendedAt, "Device suspended. Reason=" + reason));
    }

    /**
     * Da de baja definitiva el dispositivo (WF-TM04 Flujo Baja paso 1B).
     * Revoca TODOS los enrollments activos asociados al dispositivo.
     *
     * @param decomAt Hora del servidor NTP.
     * @param actorId Actor que decomisiona.
     */
    public void decommission(LocalDateTime decomAt, String actorId) {
        if (this.status == DeviceStatus.DECOMMISSIONED) {
            throw new IllegalStateException(
                    "ClockingDevice [" + deviceId + "] is already DECOMMISSIONED.");
        }
        this.status = DeviceStatus.DECOMMISSIONED;
        this.decommissionedAt = decomAt;

        // Revocar todos los enrollments activos — invariante de coherencia de estado
        enrollments.stream()
                .filter(BiometricEnrollment::isActive)
                .forEach(e -> revokeEnrollmentInternal(e, decomAt, RevocationReason.DEVICE_DECOMMISSION));

        auditLogs.add(new DeviceAuditLog(
                UUID.randomUUID(), deviceId, "DECOMMISSIONED",
                actorId, decomAt,
                "Device decommissioned. All active enrollments revoked."));
    }

    /**
     * Registra un heartbeat del dispositivo. Actualiza el estado de sincronización.
     *
     * @param now              Hora del servidor NTP.
     * @param newSyncStatus    Nuevo estado de sincronización reportado.
     * @param batteryLevel     Nivel de batería (NULL para kioscos con corriente alterna).
     * @param enrolledCount    Número de templates activos en el dispositivo local.
     */
    public void recordHeartbeat(LocalDateTime now, SyncStatus newSyncStatus,
                                 Integer batteryLevel, int enrolledCount) {
        if (this.heartbeat == null) {
            this.heartbeat = new DeviceHeartbeat(now, batteryLevel, newSyncStatus, enrolledCount);
        } else {
            this.heartbeat = new DeviceHeartbeat(now, batteryLevel, newSyncStatus, enrolledCount);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WF-TM04 — Biometric Enrollment Management
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Enrola el template biométrico de un colaborador en el dispositivo (WF-TM04 paso 2).
     *
     * <p><b>Invariante Biometric Enrollment Uniqueness:</b> Si ya existe un enrollment ACTIVE
     * del mismo (relationship_id, biometric_type) en este dispositivo, lanza
     * {@link DuplicateBiometricEnrollmentException}.
     *
     * @param relationshipId       FK opaco a Relationship (BC-01).
     * @param biometricType        Tipo de biometría (FINGERPRINT / FACIAL).
     * @param templateHash         Hash SHA-512 del template normalizado. NUNCA la imagen raw.
     * @param templateQualityScore Score de calidad (0.00 – 1.00).
     * @param enrolledAt           Hora del servidor NTP del enrolamiento.
     * @return El BiometricEnrollment creado en estado ACTIVE.
     */
    public BiometricEnrollment enrollBiometric(
            UUID relationshipId,
            BiometricType biometricType,
            String templateHash,
            BigDecimal templateQualityScore,
            LocalDateTime enrolledAt) {

        assertActive("enrollBiometric");

        // Invariante: unicidad de enrollment activo por (relationship_id, biometric_type)
        boolean alreadyActive = enrollments.stream()
                .filter(e -> e.getRelationshipId().equals(relationshipId))
                .filter(e -> e.getBiometricType() == biometricType)
                .anyMatch(BiometricEnrollment::isActive);

        if (alreadyActive) {
            throw new DuplicateBiometricEnrollmentException(deviceId, relationshipId, biometricType.name());
        }

        BiometricEnrollment enrollment = new BiometricEnrollment(
                UUID.randomUUID(), deviceId, relationshipId, biometricType,
                templateHash, templateQualityScore, enrolledAt);

        enrollments.add(enrollment);

        // Mark device as PENDING_SYNC so it syncs the new template
        if (this.heartbeat != null) {
            this.heartbeat = this.heartbeat.withSync(enrolledAt, SyncStatus.PENDING_SYNC,
                    (int) enrollments.stream().filter(BiometricEnrollment::isActive).count());
        }

        auditLogs.add(new DeviceAuditLog(
                UUID.randomUUID(), deviceId, "BIOMETRIC_ENROLLED",
                "SYSTEM", enrolledAt,
                "BiometricType=" + biometricType + " RelationshipId=" + relationshipId));

        return enrollment;
    }

    /**
     * Revoca el enrollment de un colaborador específico (WF-TM04 Flujo Baja paso 2).
     * Trigger manual (EMPLOYEE_REQUEST, FRAUD_DETECTED) o por desvinculación (EMPLOYEE_OFFBOARDING).
     *
     * @param enrollmentId     UUID del enrollment a revocar.
     * @param revokedAt        Hora NTP de la revocación.
     * @param revocationReason Razón de la revocación.
     */
    public void revokeEnrollment(UUID enrollmentId, LocalDateTime revokedAt, RevocationReason revocationReason) {
        assertActive("revokeEnrollment");
        BiometricEnrollment enrollment = findEnrollmentOrThrow(enrollmentId);
        revokeEnrollmentInternal(enrollment, revokedAt, revocationReason);
    }

    /**
     * Revoca TODOS los enrollments activos de un collaborador específico en este dispositivo.
     * Trigger: evento {@code EMPLOYEE_DEACTIVATED} de BC-01 Core (WF-TM04 paso 1A).
     *
     * @param relationshipId   FK del collaborador desvinculado.
     * @param revokedAt        Hora NTP del evento de revocación.
     */
    public void revokeAllEnrollmentsForEmployee(UUID relationshipId, LocalDateTime revokedAt) {
        enrollments.stream()
                .filter(e -> e.getRelationshipId().equals(relationshipId))
                .filter(BiometricEnrollment::isActive)
                .forEach(e -> revokeEnrollmentInternal(e, revokedAt, RevocationReason.EMPLOYEE_OFFBOARDING));

        auditLogs.add(new DeviceAuditLog(
                UUID.randomUUID(), deviceId, "BIOMETRIC_REVOKED_BULK",
                "SYSTEM", revokedAt,
                "All enrollments for RelationshipId=" + relationshipId +
                " revoked. Reason=EMPLOYEE_OFFBOARDING (EMPLOYEE_DEACTIVATED event)."));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WF-TM04 — Authentication Attempt Recording (Anti-Fraud P-TM29 / P-TM30)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra un intento de autenticación y aplica las políticas anti-fraude (P-TM29/P-TM30).
     *
     * <p><b>Non-Blocking Design:</b> el dispositivo continúa operativo independientemente
     * del resultado. Si se detecta fraude ({@code AuthResult.FRAUD_DETECTED}), se emite
     * {@link SecurityPunchIncidentEvent} y el log queda marcado como security_incident=TRUE.
     * El TimeEntry asociado NO se persiste (responsabilidad del UC).
     *
     * @param relationshipId FK opaco al colaborador.
     * @param authMethod     Método de autenticación empleado.
     * @param authResult     Resultado de la autenticación.
     * @param attemptedAt    Hora del servidor NTP del intento.
     * @return El PunchAttemptLog creado (inmutable).
     */
    public PunchAttemptLog recordAuthAttempt(
            UUID relationshipId,
            AuthMethod authMethod,
            AuthResult authResult,
            LocalDateTime attemptedAt) {

        boolean isSecurityIncident = (authResult == AuthResult.FRAUD_DETECTED)
                || (authResult == AuthResult.REVOKED_CREDENTIAL);

        // Count consecutive biometric failures for same collaborator (P-TM29 / P-TM30)
        if (authResult == AuthResult.BIOMETRIC_FAIL) {
            long consecutiveFails = punchAttemptLogs.stream()
                    .filter(a -> a.getRelationshipId().equals(relationshipId))
                    .filter(a -> a.getAuthResult() == AuthResult.BIOMETRIC_FAIL)
                    .count();
            if (consecutiveFails >= 2) { // 3rd failure → security incident
                isSecurityIncident = true;
            }
        }

        PunchAttemptLog log = new PunchAttemptLog(
                UUID.randomUUID(), deviceId, attemptedAt,
                relationshipId, authMethod, authResult, isSecurityIncident, null);

        punchAttemptLogs.add(log);

        // Emit SecurityPunchIncidentEvent for fraud or revoked credential (P-TM30)
        if (isSecurityIncident) {
            String incidentType = authResult == AuthResult.FRAUD_DETECTED
                    ? "PROXY_CLOCKING"
                    : (authResult == AuthResult.REVOKED_CREDENTIAL ? "REVOKED_CREDENTIAL" : "REPEATED_BIOMETRIC_FAIL");

            domainEvents.add(new SecurityPunchIncidentEvent(
                    log.getAttemptId(),
                    deviceId,
                    relationshipId,
                    incidentType,
                    attemptedAt.toInstant(ZoneOffset.UTC),
                    Instant.now(),
                    tenantId));
        }

        return log;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Invariante: Device Signature Integrity
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Verifica la firma digital de un TimeEntry proveniente de este dispositivo.
     * Solo aplica a KIOSK y BIOMETRIC_READER (Invariante Device Signature Integrity).
     *
     * <p>La verificación real de la firma RSA se delega a un servicio de infraestructura;
     * el AR solo valida que la firma no sea nula/vacía cuando el tipo de dispositivo la requiere.
     *
     * @param deviceSignature Firma digital del dispositivo (puede ser null para MOBILE/WEB).
     * @throws InvalidDeviceSignatureException si el dispositivo requiere firma y ésta es inválida/ausente.
     */
    public void validateSignature(String deviceSignature) {
        if (deviceType == DeviceType.KIOSK || deviceType == DeviceType.BIOMETRIC_READER) {
            if (deviceSignature == null || deviceSignature.isBlank()) {
                throw new InvalidDeviceSignatureException(deviceId);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Domain Events
    // ═══════════════════════════════════════════════════════════════════════════

    /** Returns accumulated domain events and clears the internal list (transactional outbox pattern). */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private void assertActive(String operation) {
        if (this.status != DeviceStatus.ACTIVE) {
            throw new IllegalStateException(
                    "ClockingDevice [" + deviceId + "] must be ACTIVE to perform [" + operation +
                    "]. Current status: " + status);
        }
    }

    private void revokeEnrollmentInternal(BiometricEnrollment enrollment,
                                          LocalDateTime revokedAt,
                                          RevocationReason reason) {
        enrollment.revoke(revokedAt, reason);

        domainEvents.add(new BiometricEnrollmentRevokedEvent(
                enrollment.getEnrollmentId(),
                deviceId,
                enrollment.getRelationshipId(),
                reason.name(),
                revokedAt.toInstant(ZoneOffset.UTC),
                tenantId));

        // Trigger sync
        if (this.heartbeat != null) {
            long activeCount = enrollments.stream().filter(BiometricEnrollment::isActive).count();
            this.heartbeat = this.heartbeat.withSync(revokedAt, SyncStatus.PENDING_SYNC, (int) activeCount);
        }
    }

    private BiometricEnrollment findEnrollmentOrThrow(UUID enrollmentId) {
        return enrollments.stream()
                .filter(e -> e.getEnrollmentId().equals(enrollmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "BiometricEnrollment [" + enrollmentId + "] not found in device [" + deviceId + "]."));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Getters (read-only views)
    // ═══════════════════════════════════════════════════════════════════════════

    public UUID getDeviceId()               { return deviceId; }
    public UUID getTenantId()               { return tenantId; }
    public UUID getOrgUnitId()              { return orgUnitId; }
    public String getSerialNumber()         { return serialNumber; }
    public DeviceType getDeviceType()       { return deviceType; }
    public DeviceRole getDeviceRole()       { return deviceRole; }
    public DeviceStatus getStatus()         { return status; }
    public LocalDateTime getInstalledAt()   { return installedAt; }
    public LocalDateTime getDecommissionedAt() { return decommissionedAt; }
    public DeviceCapabilities getCapabilities() { return capabilities; }
    public DeviceHeartbeat getHeartbeat()   { return heartbeat; }

    public List<BiometricEnrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }
    public List<PunchAttemptLog> getPunchAttemptLogs() {
        return Collections.unmodifiableList(punchAttemptLogs);
    }
    public List<DeviceAuditLog> getAuditLogs() {
        return Collections.unmodifiableList(auditLogs);
    }

    /** Reconstitution: loads enrollments (called by repository adapter). */
    public void loadEnrollments(List<BiometricEnrollment> items) { this.enrollments.addAll(items); }

    /** Reconstitution: loads attempt logs (called by repository adapter). */
    public void loadPunchAttemptLogs(List<PunchAttemptLog> items) { this.punchAttemptLogs.addAll(items); }

    /** Reconstitution: loads audit logs (called by repository adapter). */
    public void loadAuditLogs(List<DeviceAuditLog> items) { this.auditLogs.addAll(items); }
}
