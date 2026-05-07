package com.solveria.TimeAndBearings.application.port.inbound;

import com.solveria.TimeAndBearings.domain.model.entity.TimeEntry;
import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.core.workforce.domain.model.vo.Extension;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound Port: Real-Time Clocking (WF-TM01).
 *
 * <p>Implements Non-Blocking Design: geo/auth failures MUST NOT block the device.
 * They generate asynchronous TimeDeviationRecord and domain events.
 *
 * <p>Called by the REST controller (mobile app, kiosk API, biometric reader gateway).
 */
public interface RealTimeClockingPort {

    /**
     * Records a real-time clocking event for a collaborator (WF-TM01).
     *
     * <p>The application layer assigns {@code punch_time} from the NTP server clock.
     * Any timestamp embedded in the command is ignored (P-TM26).
     *
     * @param command Clocking intent from the channel adapter. Never contains client timestamp.
     * @return The persisted TimeEntry (immutable, append-only).
     */
    TimeEntry clock(ClockCommand command);

    /**
     * Command object for the real-time clocking use case.
     * All fields required for P-TM26, P-TM27, P-TM28.
     *
     * @param tenantId          Multi-tenant partition.
     * @param relationshipId    BC-01 Relationship reference (opaque UUID, verified active by ACL).
     * @param source            Origin channel (determines auth level per P-TM29).
     * @param deviceId          FK to ClockingDevice. NULL if source=MANUAL/WEB.
     * @param deviceSignature   Cryptographic device signature. NOT NULL for KIOSK/BIOMETRIC_READER.
     * @param ipAddress         IPv4/IPv6. Mandatory for MOBILE/WEB.
     * @param userAgent         HTTP User-Agent for audit.
     * @param latitude          GPS latitude from device. NULL for KIOSK/WEB.
     * @param longitude         GPS longitude from device. NULL for KIOSK/WEB.
     * @param accuracyMeters    GPS accuracy reported by device.
     * @param orgExtension      org_extension queried from BC-01 Core ACL [GEO-01].
     * @param remoteWorkAuthId  Active RemoteWorkAuth UUID if remote session authorized (WF-TM05).
     * @param fraudFlag         TRUE if anti-fraud engine detected proxy clocking (P-TM30).
     */
    record ClockCommand(
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
            boolean fraudFlag
    ) {}
}
