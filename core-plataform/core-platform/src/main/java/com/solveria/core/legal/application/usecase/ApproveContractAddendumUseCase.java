package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.dto.ApproveContractAddendumRequest;
import com.solveria.core.legal.application.dto.ContractAddendumResponse;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import com.solveria.core.shared.exceptions.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveContractAddendumUseCase {

  private final ContractRepositoryPort contractRepositoryPort;

  @Transactional
  public ContractAddendumResponse execute(ApproveContractAddendumRequest request) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (!tenantId.equals(request.tenantId())) {
      throw new IllegalStateException("Tenant inconsistente entre request y contexto de seguridad");
    }

    Contract contract =
        contractRepositoryPort
            .findById(request.contractId())
            .orElseThrow(
                () -> new EntityNotFoundException("Contract", request.contractId().toString()));

    ContractAddendum addendum = findAddendum(contract, request.addendumId());
    String currentUser = SecurityUserContext.getUserIdentifier();

    contract.approveAddendum(request.addendumId(), addendum.getCreatedBy(), currentUser);
    contractRepositoryPort.save(contract);

    log.info(
        "event=LEGAL_CONTRACT_ADDENDUM_APPROVED contractId={} addendumId={}",
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

  private ContractAddendum findAddendum(Contract contract, UUID addendumId) {
    return contract.getAddendums().stream()
        .filter(addendum -> addendum.getAddendumId().equals(addendumId))
        .findFirst()
        .orElseThrow(() -> new EntityNotFoundException("ContractAddendum", addendumId.toString()));
  }
}
