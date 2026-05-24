package com.solveria.core.legal.infrastructure.listener;

import com.solveria.core.legal.application.dto.ComplianceSnapshotDto;
import com.solveria.core.legal.application.dto.ProposeContractAddendumRequest;
import com.solveria.core.legal.application.dto.SalaryTermsDto;
import com.solveria.core.legal.domain.model.vo.ComplianceSnapshot;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import com.solveria.core.legal.infrastructure.jpa.ContractAddendumJpa;
import com.solveria.core.legal.infrastructure.jpa.ContractJpa;
import com.solveria.core.legal.infrastructure.mapper.ContractMapper;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContractEventMapper {
  /**
   * Construye el request para una nueva adenda basándose en la última adenda vigente del contrato.
   */
  private final ContractMapper contractMapper;

  public ProposeContractAddendumRequest toProposeAddendumRequest(ContractJpa contract) {

    ContractAddendumJpa currentAddendum =
        contract.getAddendums().stream()
            .max(Comparator.comparing(ContractAddendumJpa::getEffectiveFrom))
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "El contrato no tiene adendas base para extraer el salario."));

    SalaryTerms terms = contractMapper.toDomain(currentAddendum.getSalaryTerms());
    ComplianceSnapshot snapshot = contractMapper.toDomain(currentAddendum.getComplianceSnapshot());

    return new ProposeContractAddendumRequest(
        contract.getContractId(),
        UUID.randomUUID(),
        LocalDate.now(),
        currentAddendum.getEffectiveTo(),
        toSalaryTermsDto(terms),
        toComplianceSnapshotDto(snapshot),
        contract.getTenantId());
  }

  private SalaryTermsDto toSalaryTermsDto(SalaryTerms terms) {
    if (terms == null) return null;
    return new SalaryTermsDto(
        terms.basicSalary(), // Asumiendo que es un record o tiene el getter
        terms.totalEarnedProj(),
        terms.netSalaryProj(),
        terms.currency());
  }

  private ComplianceSnapshotDto toComplianceSnapshotDto(ComplianceSnapshot snapshot) {
    if (snapshot == null) return null;
    return new ComplianceSnapshotDto(
        snapshot.smnApplied(), // Asumiendo que es un record o tiene el getter
        snapshot.taxRegime(),
        snapshot.infocalActive());
  }
}
