package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.dto.ApproveContractRequest;
import com.solveria.core.legal.application.dto.ContractAddendumResponse;
import com.solveria.core.legal.application.dto.ContractResponse;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import com.solveria.core.shared.exceptions.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveContractUseCase {

  private final ContractRepositoryPort contractRepositoryPort;

  @Transactional
  public ContractResponse execute(ApproveContractRequest request) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();


    Contract contract =
        contractRepositoryPort
            .findById(request.contractId())
            .orElseThrow(
                () -> new EntityNotFoundException("Contract", request.contractId().toString()));

    String currentUser = SecurityUserContext.getUserIdentifier();
    contract.approve(contract.getCreatedBy(), currentUser);
    contractRepositoryPort.save(contract);

    log.info("event=LEGAL_CONTRACT_APPROVED contractId={}", contract.getContractId());

    return new ContractResponse(
        contract.getContractId(),
        contract.getRelationshipId(),
        contract.getContractType(),
        contract.getEmploymentCond(),
        contract.getStatus(),
        contract.getProjectId(),
        contract.getTenantId(),
        mapAddendums(contract.getAddendums()));
  }

  private List<ContractAddendumResponse> mapAddendums(List<ContractAddendum> addendums) {
    if (addendums == null) {
      return List.of();
    }
    return addendums.stream()
        .map(
            addendum ->
                new ContractAddendumResponse(
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
                    addendum.getSnapshot().infocalActive()))
        .toList();
  }
}
