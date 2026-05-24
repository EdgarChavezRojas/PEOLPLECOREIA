package com.solveria.TimeAndBearings.application.port.inbound;

import com.solveria.TimeAndBearings.application.command.ClockCommand;
import com.solveria.TimeAndBearings.domain.model.entity.TimeEntry;

/**
 * Inbound Port: Real-Time Clocking (WF-TM01).
 *
 * <p>Implements Non-Blocking Design: geo/auth failures MUST NOT block the device. They generate
 * asynchronous TimeDeviationRecord and domain events.
 *
 * <p>Called by the REST controller (mobile app, kiosk API, biometric reader gateway).
 */
public interface RealTimeClockingPort {

  /**
   * Records a real-time clocking event for a collaborator (WF-TM01).
   *
   * <p>The application layer assigns {@code punch_time} from the NTP server clock. Any timestamp
   * embedded in the command is ignored (P-TM26).
   *
   * @param command Clocking intent from the channel adapter. Never contains client timestamp.
   * @return The persisted TimeEntry (immutable, append-only).
   */
  TimeEntry clock(ClockCommand command);
}
