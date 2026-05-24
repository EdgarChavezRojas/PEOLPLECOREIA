package com.solveria.TimeAndBearings.application.command;

import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.core.workforce.domain.model.vo.Extension;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for the real-time clocking use case. All fields required for P-TM26, P-TM27, P-TM28.
 *
 * @param tenantId Multi-tenant partition.
 * @param relationshipId BC-01 Relationship reference (opaque UUID, verified active by ACL).
 * @param source Origin channel (determines auth level per P-TM29).
 * @param deviceId FK to ClockingDevice. NULL if source=MANUAL/WEB.
 * @param deviceSignature Cryptographic device signature. NOT NULL for KIOSK/BIOMETRIC_READER.
 * @param ipAddress IPv4/IPv6. Mandatory for MOBILE/WEB.
 * @param userAgent HTTP User-Agent for audit.
 * @param latitude GPS latitude from device. NULL for KIOSK/WEB.
 * @param longitude GPS longitude from device. NULL for KIOSK/WEB.
 * @param accuracyMeters GPS accuracy reported by device.
 * @param orgExtension org_extension queried from BC-01 Core ACL [GEO-01].
 * @param remoteWorkAuthId Active RemoteWorkAuth UUID if remote session authorized (WF-TM05).
 * @param fraudFlag TRUE if anti-fraud engine detected proxy clocking (P-TM30).
 */
public record ClockCommand(
    UUID tenantId,
    UUID relationshipId,
    PunchSource source,
    UUID deviceId,
    String deviceSignature,
    String ipAddress,
    String userAgent,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal accuracyMeters,
    Extension orgExtension,
    UUID remoteWorkAuthId,
    boolean fraudFlag) {}
