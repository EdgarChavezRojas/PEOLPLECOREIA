package com.solveria.TimeAndBearings.domain.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio (Aggregate 16: TimesheetPeriod).
 *
 * <p>Resumen calculado por el CRON nocturno (WF-TM03, paso 1) para un {@code work_date} específico
 * dentro del {@code TimesheetPeriod}. Agrega los {@code WorkedHoursSummary} de todos los
 * colaboradores de la {@code OrgUnit} para ese día.
 *
 * <p><b>Responsabilidad:</b> Ser el agregador diario que permite a BC-TM generar el {@code
 * PayrollHandoffPackage} con datos consolidados por periodo.
 *
 * <p><b>Ciclo de vida:</b> Creado y actualizado exclusivamente por el CRON del {@code
 * TimesheetPeriod} Aggregate Root. Una vez que el {@code TimesheetPeriod} transiciona a {@code
 * CLOSED}, este resumen es inmutable (P-TM33).
 */
public class DailyConsolidationSummary {

  private UUID summaryId;
  private UUID periodId;
  private LocalDate workDate;
  private int totalScheduled;
  private int totalAttended;
  private int totalNoShows;
  private int totalExceptionsPending;
  private BigDecimal totalRegularHours;
  private BigDecimal totalOvertimeHours;
  private BigDecimal totalNightHours;
  private LocalDateTime calculatedAt;

  public void setSummaryId(UUID summaryId) {
    this.summaryId = summaryId;
  }

  public void setPeriodId(UUID periodId) {
    this.periodId = periodId;
  }

  public void setWorkDate(LocalDate workDate) {
    this.workDate = workDate;
  }

  public void setTotalScheduled(int totalScheduled) {
    this.totalScheduled = totalScheduled;
  }

  public void setTotalAttended(int totalAttended) {
    this.totalAttended = totalAttended;
  }

  public void setTotalNoShows(int totalNoShows) {
    this.totalNoShows = totalNoShows;
  }

  public void setTotalExceptionsPending(int totalExceptionsPending) {
    this.totalExceptionsPending = totalExceptionsPending;
  }

  public void setTotalRegularHours(BigDecimal totalRegularHours) {
    this.totalRegularHours = totalRegularHours;
  }

  public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
    this.totalOvertimeHours = totalOvertimeHours;
  }

  public void setTotalNightHours(BigDecimal totalNightHours) {
    this.totalNightHours = totalNightHours;
  }

  public void setCalculatedAt(LocalDateTime calculatedAt) {
    this.calculatedAt = calculatedAt;
  }

  /**
   * Constructor de reconstrucción (desde persistencia).
   *
   * @param summaryId PK de la entidad
   * @param periodId FK al {@code TimesheetPeriod}
   * @param workDate día calendario del resumen
   * @param totalScheduled número de empleados con turno asignado
   * @param totalAttended número de empleados con al menos un PUNCH_IN
   * @param totalNoShows empleados con turno y cero marcaciones
   * @param totalExceptionsPending excepciones PENDING al momento del CRON
   * @param totalRegularHours suma de regular_hours de todos los WorkedHoursSummary del día
   * @param totalOvertimeHours suma de overtime_hours aprobadas del día
   * @param totalNightHours suma de night_hours del día
   * @param calculatedAt momento en que el CRON calculó este resumen
   */
  public DailyConsolidationSummary(
      UUID summaryId,
      UUID periodId,
      LocalDate workDate,
      int totalScheduled,
      int totalAttended,
      int totalNoShows,
      int totalExceptionsPending,
      BigDecimal totalRegularHours,
      BigDecimal totalOvertimeHours,
      BigDecimal totalNightHours,
      LocalDateTime calculatedAt) {
    this.summaryId = Objects.requireNonNull(summaryId, "summaryId es requerido");
    this.periodId = Objects.requireNonNull(periodId, "periodId es requerido");
    this.workDate = Objects.requireNonNull(workDate, "workDate es requerido");
    this.totalScheduled = totalScheduled;
    this.totalAttended = totalAttended;
    this.totalNoShows = totalNoShows;
    this.totalExceptionsPending = totalExceptionsPending;
    this.totalRegularHours = Objects.requireNonNullElse(totalRegularHours, BigDecimal.ZERO);
    this.totalOvertimeHours = Objects.requireNonNullElse(totalOvertimeHours, BigDecimal.ZERO);
    this.totalNightHours = Objects.requireNonNullElse(totalNightHours, BigDecimal.ZERO);
    this.calculatedAt = calculatedAt;
  }

  /**
   * Factory de creación inicial por el CRON de consolidación (WF-TM03, paso 2-3).
   *
   * @param periodId FK al {@code TimesheetPeriod} padre
   * @param workDate día calendario a resumir
   * @param serverNow timestamp del servidor NTP en el momento del cálculo
   * @return nueva instancia con contadores en cero, lista para acumulación
   */
  public static DailyConsolidationSummary create(
      UUID periodId, LocalDate workDate, LocalDateTime serverNow) {
    return new DailyConsolidationSummary(
        UUID.randomUUID(),
        periodId,
        workDate,
        0,
        0,
        0,
        0,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        serverNow);
  }

  /**
   * Actualiza los contadores con los datos calculados por el CRON. Solo puede ser llamado por el
   * {@code TimesheetPeriod} Aggregate Root.
   *
   * @param totalScheduled número de empleados con turno asignado
   * @param totalAttended número de empleados con al menos un PUNCH_IN
   * @param totalNoShows empleados con turno y cero marcaciones
   * @param totalExceptionsPending excepciones PENDING al momento del CRON
   * @param totalRegularHours suma de regular_hours del día
   * @param totalOvertimeHours suma de overtime_hours aprobadas del día
   * @param totalNightHours suma de night_hours del día
   * @param serverNow timestamp del servidor NTP (calculatedAt)
   */
  public void updateCounters(
      int totalScheduled,
      int totalAttended,
      int totalNoShows,
      int totalExceptionsPending,
      BigDecimal totalRegularHours,
      BigDecimal totalOvertimeHours,
      BigDecimal totalNightHours,
      LocalDateTime serverNow) {
    this.totalScheduled = totalScheduled;
    this.totalAttended = totalAttended;
    this.totalNoShows = totalNoShows;
    this.totalExceptionsPending = totalExceptionsPending;
    this.totalRegularHours = totalRegularHours;
    this.totalOvertimeHours = totalOvertimeHours;
    this.totalNightHours = totalNightHours;
    this.calculatedAt = serverNow;
  }

  // ── Getters (sin setters públicos: mutación controlada por el AR) ──────────

  public UUID getSummaryId() {
    return summaryId;
  }

  public UUID getPeriodId() {
    return periodId;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public int getTotalScheduled() {
    return totalScheduled;
  }

  public int getTotalAttended() {
    return totalAttended;
  }

  public int getTotalNoShows() {
    return totalNoShows;
  }

  public int getTotalExceptionsPending() {
    return totalExceptionsPending;
  }

  public BigDecimal getTotalRegularHours() {
    return totalRegularHours;
  }

  public BigDecimal getTotalOvertimeHours() {
    return totalOvertimeHours;
  }

  public BigDecimal getTotalNightHours() {
    return totalNightHours;
  }

  public LocalDateTime getCalculatedAt() {
    return calculatedAt;
  }
}
