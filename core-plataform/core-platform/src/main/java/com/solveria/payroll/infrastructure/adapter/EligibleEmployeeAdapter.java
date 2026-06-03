package com.solveria.payroll.infrastructure.adapter;

import com.solveria.core.financial.application.port.TaxForm110RepositoryPort;
import com.solveria.core.financial.domain.model.TaxForm110;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Person;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.payroll.application.dto.request.EligibleEmployee;
import com.solveria.payroll.application.port.outbound.EligibleEmployeePort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EligibleEmployeeAdapter implements EligibleEmployeePort {

  private final RelationshipRepositoryPort relationshipRepository;
  private final PersonRepositoryPort personRepository;
  private final ContractRepositoryPort contractRepository;
  private final TaxForm110RepositoryPort taxForm110Repository;
  private static final int DEFAULT_SENIORITY_YEARS = 30;

  @Override
  public List<EligibleEmployee> findEligibleByTenantId(UUID tenantId) {
    // 1. Obtener los vínculos laborales que pertenezcan al tenant y estén estrictamente ACTIVOS
    List<Relationship> activeRelationships =
        relationshipRepository.findAll().stream()
            .filter(r -> r.getTenantId().equals(tenantId))
            .filter(r -> RelationshipStatus.ACTIVE.equals(r.getCurrentStatus()))
            .toList();

    return activeRelationships.stream().map(r -> mapToEligibleEmployee(r, tenantId)).toList();
  }

  @Override
  public EligibleEmployee findById(UUID employeeId, UUID tenantId) {
    // Optimización de rendimiento: Búsqueda directa y dirigida para boletas individuales, evitando
    // scans completos
    Relationship relationship =
        relationshipRepository
            .findByRelationshipIdAndTenantId(employeeId, tenantId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No se encontró un vínculo laboral activo para el ID proporcionado."));

    if (!RelationshipStatus.ACTIVE.equals(relationship.getCurrentStatus())) {
      throw new IllegalStateException("El colaborador no se encuentra en estado ACTIVO.");
    }

    return mapToEligibleEmployee(relationship, tenantId);
  }

  // ── Método de ayuda privado para poblar datos reales sin Hardcode ─────────────────
  private EligibleEmployee mapToEligibleEmployee(Relationship r, UUID tenantId) {
    // 1. Obtener la Persona (Identidad) asociada al vínculo laboral
    Person person =
        personRepository
            .findByPersonId(r.getPersonId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Invariante roto: La relación "
                            + r.getRelationshipId()
                            + " no posee una entidad Person válida."));

    // 2. Obtener el Contrato vigente y aprobado a través del puerto secundario de Legal
    Contract contract =
        contractRepository
            .findByRelationshipId(r.getRelationshipId())
            .flatMap(
                jpa ->
                    contractRepository.findById(
                        jpa.getContractId())) // Conversión de JPA a Agregado de Dominio
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No se encontró un contrato legal configurado para el colaborador: "
                            + r.getRelationshipId()));

    if (ContractStatus.TERMINATED.equals(contract.getStatus())) {
      throw new IllegalStateException(
          "El contrato del colaborador se encuentra rescindido o terminado.");
    }

    // 3. Extraer el salario básico vigente buscando en el addendum aprobado más reciente
    BigDecimal realBasicSalary =
        contract.getAddendums().stream()
            .filter(a -> a.getSalaryTerms() != null)
            .max(Comparator.comparing(ContractAddendum::getEffectiveFrom))
            .map(a -> a.getSalaryTerms().basicSalary())
            .orElse(BigDecimal.ZERO); // Fallback matemático seguro si no tiene addendums aún

    // 4. Calcular la antigüedad real según la Ley General del Trabajo de Bolivia
    int seniorityYears =
        r.getHireDate() != null
            ? (int) ChronoUnit.YEARS.between(r.getHireDate(), LocalDate.now())
            : 0;

    // 5. Consolidar el Crédito Fiscal verificado del Formulario 110 para el periodo actual (SIAT
    // RC-IVA)
    // El puerto de Financial pide tenantId como String, respetamos su firma exacta para compilar
    BigDecimal currentFiscalCredit =
        taxForm110Repository
            .findByPersonIdAndPeriod(person.getPersonId(), YearMonth.now(), tenantId.toString())
            .stream()
            .map(TaxForm110::getVerifiedCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 6. Retornar el DTO mapeado con datos operacionales verdaderos del Core
    return new EligibleEmployee(
        r.getRelationshipId(),
        person.getFullName(),
        person.getDNI(), // Mapea el DNI/CI legítimo de la persona
        r.getRelationType() != null ? r.getRelationType().name() : "PLANILLA_MENSUAL",
        person.getProfessionTitle() != null ? person.getProfessionTitle() : "Colaborador",
        realBasicSalary,
        seniorityYears,
        currentFiscalCredit,
        r.getHireDate() != null ? r.getHireDate() : LocalDate.now(),
        DEFAULT_SENIORITY_YEARS // Días comerciales estándar para el cálculo del periodo activo
        );
  }
}
