package com.solveria.TimeAndBearings.domain.model.entity;

import com.solveria.TimeAndBearings.domain.model.vo.EmployeeHandoffRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad inmutable de dominio (Aggregate 16: TimesheetPeriod).
 *
 * <p>El paquete de datos final generado al cerrar el {@code TimesheetPeriod}. Contiene el resumen
 * validado por empleado, listo para BC-05 (Payroll).
 *
 * <p><b>Responsabilidad:</b> Ser el contrato formal entre BC-TM y BC-05. BC-05 solo lee este
 * paquete vía el evento {@code ATTENDANCE_PERIOD_CLOSED}; nunca accede directamente a los {@code
 * AttendanceLedger} individuales.
 *
 * <p><b>Invariante (P-TM33):</b> Una vez creado es completamente inmutable. No existen métodos de
 * mutación; el checksum SHA-512 se calcula en el constructor de factory para garantizar la
 * integridad del payload.
 *
 * <p><b>Checksum (Scratchpad):</b> Generado con SHA-512 sobre la representación canónica del
 * payload ({@code periodId + generatedAt + todos los registros}) para que BC-05 pueda verificar la
 * integridad del paquete recibido.
 */
public class PayrollHandoffPackage {

  private UUID handoffId;
  private UUID periodId;
  private LocalDateTime generatedAt;
  private String checksum;
  private List<EmployeeHandoffRecord> employeeRecords;

  public void setHandoffId(UUID handoffId) {
    this.handoffId = handoffId;
  }

  public void setPeriodId(UUID periodId) {
    this.periodId = periodId;
  }

  public void setGeneratedAt(LocalDateTime generatedAt) {
    this.generatedAt = generatedAt;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public void setEmployeeRecords(List<EmployeeHandoffRecord> employeeRecords) {
    this.employeeRecords = employeeRecords;
  }

  /**
   * Constructor de reconstrucción (desde persistencia). No recalcula el checksum; lo usa tal como
   * fue almacenado.
   *
   * @param handoffId PK de la entidad
   * @param periodId FK al {@code TimesheetPeriod}
   * @param generatedAt momento de generación (inmutable)
   * @param checksum hash SHA-512 del payload almacenado
   * @param employeeRecords lista de registros por empleado
   */
  public PayrollHandoffPackage(
      UUID handoffId,
      UUID periodId,
      LocalDateTime generatedAt,
      String checksum,
      List<EmployeeHandoffRecord> employeeRecords) {
    this.handoffId = Objects.requireNonNull(handoffId, "handoffId es requerido");
    this.periodId = Objects.requireNonNull(periodId, "periodId es requerido");
    this.generatedAt = Objects.requireNonNull(generatedAt, "generatedAt es requerido");
    this.checksum = Objects.requireNonNull(checksum, "checksum es requerido");
    this.employeeRecords =
        List.copyOf(Objects.requireNonNull(employeeRecords, "employeeRecords es requerido"));
  }

  /**
   * Factory de creación al finalizar el cierre del {@code TimesheetPeriod} (WF-TM03, paso 7).
   *
   * <p>Calcula el checksum SHA-512 sobre la representación canónica del payload: {@code periodId +
   * generatedAt.toString() +
   * [relationshipId:regular:overtime:night:holiday:absences:remote:flag]...}
   *
   * @param periodId FK al {@code TimesheetPeriod} cerrado
   * @param serverNow timestamp del servidor NTP en el momento de generación
   * @param employeeRecords lista completa de {@code EmployeeHandoffRecord} del periodo
   * @return nueva instancia inmutable con checksum calculado
   */
  public static PayrollHandoffPackage create(
      UUID periodId, LocalDateTime serverNow, List<EmployeeHandoffRecord> employeeRecords) {
    Objects.requireNonNull(periodId, "periodId es requerido");
    Objects.requireNonNull(serverNow, "serverNow es requerido");
    Objects.requireNonNull(employeeRecords, "employeeRecords es requerido");

    List<EmployeeHandoffRecord> immutableRecords = List.copyOf(employeeRecords);
    String checksum = computeChecksum(periodId, serverNow, immutableRecords);

    return new PayrollHandoffPackage(
        UUID.randomUUID(), periodId, serverNow, checksum, immutableRecords);
  }

  /**
   * Calcula el hash SHA-512 del payload del paquete.
   *
   * <p>Representación canónica usada para el hash:
   *
   * <pre>
   *   {periodId}|{generatedAt}|{r1.relationshipId}:{r1.regular}:{r1.overtime}:...
   *   |{r2.relationshipId}:...
   * </pre>
   *
   * @param periodId id del periodo
   * @param generatedAt timestamp de generación
   * @param employeeRecords registros de empleados
   * @return checksum SHA-512 en formato hexadecimal (128 caracteres)
   */
  private static String computeChecksum(
      UUID periodId, LocalDateTime generatedAt, List<EmployeeHandoffRecord> employeeRecords) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512");
      StringBuilder sb = new StringBuilder();
      sb.append(periodId).append('|').append(generatedAt).append('|');
      for (EmployeeHandoffRecord r : employeeRecords) {
        sb.append(r.relationshipId())
            .append(':')
            .append(r.regularHoursTotal())
            .append(':')
            .append(r.overtimeHoursTotal())
            .append(':')
            .append(r.nightHoursTotal())
            .append(':')
            .append(r.holidayHoursTotal())
            .append(':')
            .append(r.unjustifiedAbsences())
            .append(':')
            .append(r.remoteWorkDays())
            .append(':')
            .append(r.dataQualityFlag().name())
            .append('|');
      }
      byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-512 no disponible en el JVM", e);
    }
  }

  // ── Getters (inmutable: sin setters) ──────────────────────────────────────

  public UUID getHandoffId() {
    return handoffId;
  }

  public UUID getPeriodId() {
    return periodId;
  }

  public LocalDateTime getGeneratedAt() {
    return generatedAt;
  }

  public String getChecksum() {
    return checksum;
  }

  /**
   * @return vista inmutable de los registros por empleado.
   */
  public List<EmployeeHandoffRecord> getEmployeeRecords() {
    return Collections.unmodifiableList(employeeRecords);
  }
}
