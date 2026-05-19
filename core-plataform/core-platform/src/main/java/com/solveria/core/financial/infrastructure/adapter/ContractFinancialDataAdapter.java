package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.command.ProcessLiquidationCommand;
import com.solveria.core.financial.application.port.ContractFinancialDataPort;
import com.solveria.core.financial.application.port.FundingSourceRepositoryPort;
import com.solveria.core.financial.domain.model.FundingSource;
import com.solveria.core.financial.domain.model.vo.TerminationType;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter: Resuelve datos financieros de contratos consultando los BCs de Legal y Workforce.
 *
 * <p>Implementa el patrón ACL (Anti-Corruption Layer) para que el listener de Financial no
 * dependa directamente de los modelos de dominio de otros BCs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractFinancialDataAdapter implements ContractFinancialDataPort {

  private final ContractRepositoryPort contractRepository;
  private final RelationshipRepositoryPort relationshipRepository;
  private final FundingSourceRepositoryPort fundingSourceRepository;

  @Override
  public ProcessLiquidationCommand buildLiquidationCommand(UUID contractId) {
    log.info("event=BUILD_LIQUIDATION_COMMAND contractId={}", contractId);

    Contract contract = findContractOrThrow(contractId);
    Relationship relationship = findRelationshipByContract(contract);

    // Extraer salarios de los últimos addendums aprobados (o del contrato base)
    List<BigDecimal> lastThreeMonthsSalaries = extractLastThreeMonthsSalaries(contract);
    List<BigDecimal> lastThreeMonthsBase = extractLastThreeMonthsBase(contract);
    List<BigDecimal> lastThreeMonthsOthers = extractLastThreeMonthsOthers(contract);

    // Determinar tipo de terminación según el estado del contrato
    TerminationType terminationType = resolveTerminationType(contract);

    return new ProcessLiquidationCommand(
        contract.getRelationshipId(),
        relationship.getPersonId(),
        terminationType,
        LocalDate.now(),
        relationship.getHireDate(),
        lastThreeMonthsSalaries,
        lastThreeMonthsBase,
        lastThreeMonthsOthers,
        0, // pendingVacationDays: se resuelve dentro del UC con el BC de Accruals
        contract.getTenantId(),
        contract.getCreatedBy());
  }

  @Override
  public BigDecimal getRequiredBudgetForContract(UUID contractId) {
    Contract contract = findContractOrThrow(contractId);
    SalaryTerms salaryTerms = resolveCurrentSalaryTerms(contract);
    return salaryTerms != null && salaryTerms.totalEarnedProj() != null
        ? salaryTerms.totalEarnedProj()
        : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getAllocationAmountForContract(UUID contractId) {
    Contract contract = findContractOrThrow(contractId);
    SalaryTerms salaryTerms = resolveCurrentSalaryTerms(contract);
    return salaryTerms != null && salaryTerms.totalEarnedProj() != null
        ? salaryTerms.totalEarnedProj()
        : BigDecimal.ZERO;
  }

  @Override
  public UUID getFundingSourceIdForContract(UUID contractId) {
    Contract contract = findContractOrThrow(contractId);
    UUID projectId = contract.getProjectId();

    if (projectId == null) {
      throw new IllegalStateException(
          "El contrato " + contractId + " no tiene projectId asociado");
    }

    // Buscar FundingSource por projectCode derivado del projectId del contrato
    return fundingSourceRepository
        .findById(projectId)
        .map(FundingSource::getSourceId)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "FundingSource no encontrado para projectId=" + projectId));
  }

  @Override
  public String getApproverForContract(UUID contractId) {
    Contract contract = findContractOrThrow(contractId);
    // El createdBy del contrato NO puede ser el aprobador (SoD),
    // retornamos el createdBy como referencia para que el UC valide SoD internamente
    return contract.getCreatedBy();
  }

  // ── Métodos privados de resolución ───────────────────────────────────────

  private Contract findContractOrThrow(UUID contractId) {
    return contractRepository
        .findById(contractId)
        .orElseThrow(
            () -> new IllegalStateException("Contrato no encontrado: " + contractId));
  }

  private Relationship findRelationshipByContract(Contract contract) {
    List<Relationship> relationships =
        relationshipRepository.findByPersonId(contract.getRelationshipId());

    // Si findByPersonId no encuentra (porque relationshipId != personId),
    // intentar buscar directamente
    if (relationships.isEmpty()) {
      log.warn(
          "event=RELATIONSHIP_LOOKUP_FALLBACK contractId={} relationshipId={}",
          contract.getContractId(),
          contract.getRelationshipId());

      // Fallback: construir un relationship mínimo con los datos del contrato utilizando setters
      Relationship fallback = new Relationship();
      fallback.setRelationshipId(contract.getRelationshipId());
      fallback.setPersonId(contract.getRelationshipId());
      fallback.setTenantId(contract.getTenantId());
      fallback.setHireDate(LocalDate.now());
      fallback.setCurrentStatus(RelationshipStatus.TERMINATED);
      return fallback;
    }

    return relationships.stream()
        .filter(r -> RelationshipStatus.ACTIVE.equals(r.getCurrentStatus())
            || RelationshipStatus.TERMINATED.equals(r.getCurrentStatus()))
        .findFirst()
        .orElse(relationships.getFirst());
  }

  /**
   * Resuelve los SalaryTerms vigentes del contrato. Busca en los addendums aprobados más
   * recientes; si no hay, retorna null.
   */
  private SalaryTerms resolveCurrentSalaryTerms(Contract contract) {
    if (contract.getAddendums() == null || contract.getAddendums().isEmpty()) {
      return null;
    }
    return contract.getAddendums().stream()
        .filter(a -> a.getSalaryTerms() != null)
        .max(Comparator.comparing(ContractAddendum::getEffectiveFrom))
        .map(ContractAddendum::getSalaryTerms)
        .orElse(null);
  }

  private List<BigDecimal> extractLastThreeMonthsSalaries(Contract contract) {
    SalaryTerms terms = resolveCurrentSalaryTerms(contract);
    if (terms == null || terms.totalEarnedProj() == null) {
      return List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    // Proyección: se usa el total ganado proyectado como proxy para los 3 últimos meses
    BigDecimal monthlySalary = terms.totalEarnedProj();
    return List.of(monthlySalary, monthlySalary, monthlySalary);
  }

  private List<BigDecimal> extractLastThreeMonthsBase(Contract contract) {
    SalaryTerms terms = resolveCurrentSalaryTerms(contract);
    if (terms == null || terms.basicSalary() == null) {
      return List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    BigDecimal baseSalary = terms.basicSalary();
    return List.of(baseSalary, baseSalary, baseSalary);
  }

  private List<BigDecimal> extractLastThreeMonthsOthers(Contract contract) {
    SalaryTerms terms = resolveCurrentSalaryTerms(contract);
    if (terms == null || terms.totalEarnedProj() == null || terms.basicSalary() == null) {
      return List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    BigDecimal others = terms.totalEarnedProj().subtract(terms.basicSalary());
    return List.of(others, others, others);
  }

  /**
   * Mapea el tipo de contrato del BC Legal al TerminationType del BC Financial. Por defecto
   * asume renuncia voluntaria; en producción esto vendría enriquecido con el motivo real.
   */
  private TerminationType resolveTerminationType(Contract contract) {
    return switch (contract.getContractType()) {
      case PLAZO_FIJO -> TerminationType.FIN_CONTRATO_PLAZO_FIJO;
      case INDEFINIDO -> TerminationType.RENUNCIA_VOLUNTARIA;
      case OBRA -> TerminationType.FIN_CONTRATO_PLAZO_FIJO;
    };
  }
}
