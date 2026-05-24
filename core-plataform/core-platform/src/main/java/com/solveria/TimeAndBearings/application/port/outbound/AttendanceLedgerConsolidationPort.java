package com.solveria.TimeAndBearings.application.port.outbound;

import com.solveria.TimeAndBearings.application.dto.DailyStats;
import com.solveria.TimeAndBearings.application.dto.EmployeeDailySummary;
import com.solveria.TimeAndBearings.application.dto.EmployeePeriodSummary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Extension of the Outbound Port for {@code AttendanceLedger} (Aggregate 14) providing aggregation
 * and batch-closure operations required by WF-TM03 (Timesheet Consolidation Use Case — Aggregate
 * 16).
 *
 * <p>This interface is intentionally separate from {@link AttendanceLedgerRepositoryPort} to
 * preserve the APPEND-ONLY constraint on existing code while cleanly adding the
 * consolidation-specific query projections.
 *
 * <p>The infrastructure adapter ({@code AttendanceLedgerRepositoryAdapter}) must implement both
 * interfaces.
 */
public interface AttendanceLedgerConsolidationPort {

  /**
   * Computes aggregate attendance statistics for all {@code AttendanceLedger} of an OrgUnit for a
   * specific {@code workDate}.
   *
   * <p>Used by the CRON nightly job (WF-TM03, step 2-3) to build the {@code
   * DailyConsolidationSummary}.
   *
   * @param orgUnitId FK to OrgUnit (BC-01)
   * @param workDate day to compute statistics for
   * @return aggregated daily statistics
   */
  DailyStats computeDailyStats(UUID orgUnitId, LocalDate workDate);

  /**
   * Computes the accumulated {@code WorkedHoursSummary} per employee for the entire period range,
   * used to build {@code EmployeeHandoffRecord} VOs.
   *
   * <p>Used at period close time (WF-TM03, step 7) to populate the {@code PayrollHandoffPackage}.
   *
   * @param orgUnitId FK to OrgUnit (BC-01)
   * @param periodStart first day of the period (inclusive)
   * @param periodEnd last day of the period (inclusive)
   * @return list of per-employee period summaries
   */
  List<EmployeePeriodSummary> computeEmployeePeriodSummaries(
      UUID orgUnitId, LocalDate periodStart, LocalDate periodEnd);

  /**
   * Forces bulk auto-closure of all {@code AttendanceLedger} in the period range that are NOT yet
   * {@code CLOSED}.
   *
   * <p>Implements the P-TM31 / P-TM34 mandatory auto-closure:
   *
   * <ul>
   *   <li>Transitions all pending {@code TimeDeviationRecord} to {@code
   *       AUTO_CLOSED_AS_UNJUSTIFIED}.
   *   <li>Transitions all pending {@code AttendanceLedger} to {@code CLOSED} with {@code
   *       is_finalized = TRUE}.
   * </ul>
   *
   * <p>Must be called BEFORE invoking {@code TimesheetPeriod.autoClosePeriod()} so that the
   * Aggregate Root receives {@code pendingLedgersCount = 0}.
   *
   * @param periodId FK to the {@code TimesheetPeriod} being closed
   * @param periodStart first day of the period range
   * @param periodEnd last day of the period range
   * @param serverNow timestamp of the server NTP (used as {@code closed_at})
   * @return number of ledgers that were force-closed by this operation
   */
  int forceAutoClosePendingLedgers(
      UUID periodId, LocalDate periodStart, LocalDate periodEnd, LocalDateTime serverNow);

  /**
   * Computes per-employee daily attendance summaries for a specific workDate, including the 30-day
   * rolling attendance rate.
   *
   * <p>Used by the CRON nightly job (WF-TM03) to emit {@code ATTENDANCE_SUMMARY_FOR_ROSTER} events
   * toward BC-SCH Scheduling.
   *
   * @param orgUnitId FK to OrgUnit (BC-01)
   * @param workDate day to compute per-employee summaries for
   * @return list of per-employee daily summaries
   */
  List<EmployeeDailySummary> computeEmployeeDailySummaries(UUID orgUnitId, LocalDate workDate);
}
