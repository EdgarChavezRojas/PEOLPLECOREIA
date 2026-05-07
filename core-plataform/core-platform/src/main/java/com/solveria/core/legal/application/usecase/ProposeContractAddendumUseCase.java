package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.dto.ComplianceSnapshotDto;
import com.solveria.core.legal.application.dto.ContractAddendumResponse;
import com.solveria.core.legal.application.dto.ProposeContractAddendumRequest;
import com.solveria.core.legal.application.dto.SalaryTermsDto;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.application.port.PolicyRuleRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.legal.domain.model.PolicyRule;
import com.solveria.core.legal.domain.model.vo.ComplianceSnapshot;
import com.solveria.core.legal.domain.model.vo.LegalThreshold;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import com.solveria.core.shared.exceptions.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProposeContractAddendumUseCase {

  private final ContractRepositoryPort contractRepositoryPort;
  private final PolicyRuleRepositoryPort policyRuleRepositoryPort;

  @Value("${legal.policies.smn.id}")
  private String smnPolicyId;

  @Transactional
  public ContractAddendumResponse execute(ProposeContractAddendumRequest request) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (!tenantId.equals(request.tenantId())) {
      throw new IllegalStateException("Tenant inconsistente entre request y contexto de seguridad");
    }

    Contract contract =
        contractRepositoryPort
            .findById(request.contractId())
            .orElseThrow(
                () -> new EntityNotFoundException("Contract", request.contractId().toString()));

    UUID addendumId = request.addendumId() != null ? request.addendumId() : UUID.randomUUID();
    SalaryTerms salaryTerms = toSalaryTerms(request.salaryTerms());
    ComplianceSnapshot snapshot = toComplianceSnapshot(request.complianceSnapshot());
    String createdBy = SecurityUserContext.getUserIdentifier();
    BigDecimal salaryFloor = resolveSalaryFloor();

    ContractAddendum addendum =
        contract.proposeAddendum(
            addendumId,
            request.effectiveFrom(),
            request.effectiveTo(),
            salaryTerms,
            snapshot,
            createdBy,
            salaryFloor);

    contractRepositoryPort.save(contract);

    log.info(
        "event=LEGAL_CONTRACT_ADDENDUM_PROPOSED contractId={} addendumId={}",
        contract.getContractId(),
        addendum.getAddendumId());

    return new ContractAddendumResponse(
        addendum.getAddendumId(),
        addendum.getStatus(),
        addendum.getEffectiveFrom(),
        addendum.getEffectiveTo(),
        addendum.getSalaryTerms().basicSalary(),
        addendum.getSalaryTerms().totalEarnedProj(),
        addendum.getSalaryTerms().netSalaryProj(),
        addendum.getSalaryTerms().currency(),
        addendum.getSnapshot().smnApplied(),
        addendum.getSnapshot().taxRegime(),
        addendum.getSnapshot().infocalActive());
  }

  private BigDecimal resolveSalaryFloor() {
    PolicyRule policyRule =
        policyRuleRepositoryPort
            .findById(UUID.fromString(smnPolicyId))
            .orElseThrow(() -> new EntityNotFoundException("PolicyRule", smnPolicyId));
    LegalThreshold threshold =
        policyRule.getThresholds().stream()
            .max(
                Comparator.comparing(
                    LegalThreshold::effectiveDate, Comparator.nullsLast(Comparator.naturalOrder())))
            .orElseThrow(() -> new EntityNotFoundException("LegalThreshold", smnPolicyId));
    return threshold.thresholdValue();
  }

  private SalaryTerms toSalaryTerms(SalaryTermsDto dto) {
    if (dto == null) {
      return new SalaryTerms(null, null, null, null);
    }
    return new SalaryTerms(
        dto.basicSalary(), dto.totalEarnedProj(), dto.netSalaryProj(), dto.currency());
  }

  private ComplianceSnapshot toComplianceSnapshot(ComplianceSnapshotDto dto) {
    if (dto == null) {
      return new ComplianceSnapshot(null, null, null);
    }
    return new ComplianceSnapshot(dto.smnApplied(), dto.taxRegime(), dto.infocalActive());
  }
}
