package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity: {@code payroll_handoff_package} — Persistencia del contrato inmutable entre BC-TM y
 * BC-05 (Aggregate 16: PayrollHandoffPackage).
 *
 * <p><b>Cardinalidad:</b> Un único handoff por periodo ({@code UNIQUE period_id}).
 *
 * <p><b>EmployeeHandoffRecord:</b> El VO es una colección de registros embebidos almacenada en la
 * tabla {@code payroll_handoff_employee_record} via {@code @ElementCollection} + {@code @Embedded}.
 * Cada registro es un {@link EmployeeHandoffRecordEmbeddable}.
 *
 * <p>Una vez generado, este paquete nunca es modificado (P-TM33). El campo {@code updatable =
 * false} en todos los campos enforce la inmutabilidad a nivel de JPA.
 */
@Entity
@Table(
    name = "payroll_handoff_package",
    uniqueConstraints =
        @UniqueConstraint(name = "uq_payroll_handoff_period", columnNames = "period_id"))
public class PayrollHandoffPackageJpa extends BaseEntity {
  @Id
  @Column(
      name = "handoff_id",
      nullable = false,
      unique = true,
      updatable = false,
      columnDefinition = "UUID")
  private UUID handoffId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "period_id", nullable = false, updatable = false)
  private TimesheetPeriodJpa timesheetPeriod;

  @Column(name = "generated_at", nullable = false, updatable = false)
  private LocalDateTime generatedAt;

  /** Hash SHA-512 del payload completo (128 hex characters). Inmutable. */
  @Column(name = "checksum", nullable = false, updatable = false, length = 128)
  private String checksum;

  /**
   * Colección de EmployeeHandoffRecord VOs.
   *
   * <p>Mapeados como {@code @ElementCollection} en la tabla {@code
   * payroll_handoff_employee_record}, con FK {@code handoff_id}. Cada fila es un {@link
   * EmployeeHandoffRecordEmbeddable}.
   */
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "payroll_handoff_employee_record",
      joinColumns = @JoinColumn(name = "handoff_id", referencedColumnName = "handoff_id"))
  private List<EmployeeHandoffRecordEmbeddable> employeeRecords = new ArrayList<>();

  public PayrollHandoffPackageJpa() {
    // JPA
  }

  // ── Getters & Setters ─────────────────────────────────────────────────────

  public UUID getHandoffId() {
    return handoffId;
  }

  public void setHandoffId(UUID handoffId) {
    this.handoffId = handoffId;
  }

  public TimesheetPeriodJpa getTimesheetPeriod() {
    return timesheetPeriod;
  }

  public void setTimesheetPeriod(TimesheetPeriodJpa timesheetPeriod) {
    this.timesheetPeriod = timesheetPeriod;
  }

  public LocalDateTime getGeneratedAt() {
    return generatedAt;
  }

  public void setGeneratedAt(LocalDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public List<EmployeeHandoffRecordEmbeddable> getEmployeeRecords() {
    return employeeRecords;
  }

  public void setEmployeeRecords(List<EmployeeHandoffRecordEmbeddable> employeeRecords) {
    this.employeeRecords = employeeRecords;
  }
}
