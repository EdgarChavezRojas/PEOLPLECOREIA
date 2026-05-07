package com.solveria.TimeAndBearings.domain.model.vo;

import com.solveria.TimeAndBearings.domain.model.enums.DataQualityFlag;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * VO inmutable que representa el resumen de horas y ausencias de UN empleado
 * dentro del {@code PayrollHandoffPackage} (Aggregate 16).
 *
 * <p>Es el contrato formal entre BC-TM y BC-05 (Financial &amp; Payroll).
 * BC-05 consume esta estructura vía el evento {@code ATTENDANCE_PERIOD_CLOSED}
 * y NUNCA accede directamente a los {@code AttendanceLedger} individuales.
 *
 * <p><b>Atributos (según diccionario de datos):</b>
 * <ul>
 *   <li>{@code relationshipId}       – FK opaca a Relationship en BC-01. NOT NULL.</li>
 *   <li>{@code regularHoursTotal}    – Acumulado del periodo de horas ordinarias. NOT NULL.</li>
 *   <li>{@code overtimeHoursTotal}   – Acumulado de horas extras aprobadas. NOT NULL.</li>
 *   <li>{@code nightHoursTotal}      – Acumulado de horas nocturnas (22:00-06:00, recargo 25% LGT Bolivia). NOT NULL.</li>
 *   <li>{@code holidayHoursTotal}    – Acumulado de horas en festivos (recargo 100% LGT Bolivia). NOT NULL.</li>
 *   <li>{@code unjustifiedAbsences} – Número de días con ausencia injustificada. Impacta descuento en nómina.</li>
 *   <li>{@code remoteWorkDays}       – Número de días trabajados en modalidad remota autorizada.</li>
 *   <li>{@code dataQualityFlag}      – COMPLETE o PARTIAL_AUTO_CLOSED (P-TM31/P-TM34).</li>
 * </ul>
 *
 * <p><b>Invariante:</b> Todos los acumulados de horas deben ser ≥ 0.
 */
public record EmployeeHandoffRecord(
        UUID relationshipId,
        BigDecimal regularHoursTotal,
        BigDecimal overtimeHoursTotal,
        BigDecimal nightHoursTotal,
        BigDecimal holidayHoursTotal,
        int unjustifiedAbsences,
        int remoteWorkDays,
        DataQualityFlag dataQualityFlag) {

    /** Guard clause: valida invariantes de dominio en construcción. */
    public EmployeeHandoffRecord {
        Objects.requireNonNull(relationshipId, "relationshipId es requerido");
        Objects.requireNonNull(regularHoursTotal, "regularHoursTotal es requerido");
        Objects.requireNonNull(overtimeHoursTotal, "overtimeHoursTotal es requerido");
        Objects.requireNonNull(nightHoursTotal, "nightHoursTotal es requerido");
        Objects.requireNonNull(holidayHoursTotal, "holidayHoursTotal es requerido");
        Objects.requireNonNull(dataQualityFlag, "dataQualityFlag es requerido");

        if (regularHoursTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("regularHoursTotal no puede ser negativo");
        }
        if (overtimeHoursTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("overtimeHoursTotal no puede ser negativo");
        }
        if (nightHoursTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("nightHoursTotal no puede ser negativo");
        }
        if (holidayHoursTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("holidayHoursTotal no puede ser negativo");
        }
        if (unjustifiedAbsences < 0) {
            throw new IllegalArgumentException("unjustifiedAbsences no puede ser negativo");
        }
        if (remoteWorkDays < 0) {
            throw new IllegalArgumentException("remoteWorkDays no puede ser negativo");
        }
    }
}
