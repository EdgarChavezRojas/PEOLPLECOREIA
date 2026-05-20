package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.port.QuinquenioSalaryDataPort;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter: Resuelve datos salariales históricos y antigüedad para el cálculo de quinquenio.
 *
 * <p>Consulta el BC de Workforce para obtener la relación activa del empleado y calcular los meses
 * continuos de antigüedad. Los datos salariales detallados se derivan del Employment Condition de
 * la relación.
 *
 * <p>En una versión futura, este adapter puede consultar directamente la base de datos de nómina
 * (Payroll BC) para obtener boletas salariales reales de los últimos 3 meses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuinquenioSalaryDataAdapter implements QuinquenioSalaryDataPort {

  private final RelationshipRepositoryPort relationshipRepository;

  @Override
  public List<BigDecimal> getLastThreeMonthsBase(UUID personId) {
    log.info("event=RESOLVE_SALARY_BASE personId={}", personId);

    Relationship relationship = findActiveRelationship(personId);

    // Extraer sueldo base del EmploymentCondition de la relación.
    // Si la relación tiene EmploymentCondition con datos salariales vinculados,
    // se usarán; de lo contrario se retorna un marcador de cero para que el UC
    // procese con los datos que tenga disponibles.
    BigDecimal baseSalary = resolveBaseSalary(relationship);
    return List.of(baseSalary, baseSalary, baseSalary);
  }

  @Override
  public List<BigDecimal> getLastThreeMonthsOthers(UUID personId) {
    log.info("event=RESOLVE_SALARY_OTHERS personId={}", personId);

    // Los "otros conceptos" (bonos, horas extra, etc.) no están modelados en el
    // Relationship de Workforce. Retornamos ZERO; en producción este adapter
    // consultaría el Payroll BC para obtener los conceptos variables reales.
    return List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  @Override
  public int getContinuousMonths(UUID personId) {
    log.info("event=RESOLVE_CONTINUOUS_MONTHS personId={}", personId);

    Relationship relationship = findActiveRelationship(personId);
    LocalDate hireDate = relationship.getHireDate();

    if (hireDate == null) {
      log.warn("event=HIRE_DATE_NOT_FOUND personId={}", personId);
      return 0;
    }

    long months = ChronoUnit.MONTHS.between(hireDate, LocalDate.now());
    log.info("event=CONTINUOUS_MONTHS_CALCULATED personId={} months={}", personId, months);
    return (int) months;
  }

  // ── Métodos privados de resolución ───────────────────────────────────────

  private Relationship findActiveRelationship(UUID personId) {
    List<Relationship> relationships = relationshipRepository.findByPersonId(personId);

    if (relationships.isEmpty()) {
      throw new IllegalStateException("No se encontró relación laboral para personId=" + personId);
    }

    return relationships.stream()
        .filter(r -> RelationshipStatus.ACTIVE.equals(r.getCurrentStatus()))
        .findFirst()
        .orElse(relationships.getFirst());
  }

  /**
   * Resuelve el sueldo base desde la relación laboral. Si existe un EmploymentCondition vinculado
   * con datos de contrato, se podría extraer el basicSalary. En esta versión se retorna ZERO como
   * indicador de que el dato debe ser enriquecido por Payroll BC.
   */
  private BigDecimal resolveBaseSalary(Relationship relationship) {
    // El Relationship de Workforce no almacena directamente el salario base.
    // Este es un punto de extensión: cuando el Payroll BC tenga un port expuesto,
    // se consultará aquí para obtener el salario real.
    log.info(
        "event=BASE_SALARY_RESOLVED_FROM_RELATIONSHIP relationshipId={}",
        relationship.getRelationshipId());
    return BigDecimal.ZERO;
  }
}
