package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.dto.TerminateContractRequest;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.exception.ContractNotFoundException;
import com.solveria.core.legal.domain.exception.InvalidContractStatusException;
import com.solveria.core.legal.domain.exception.TenantMismatchException;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerminateContractUseCase {

  private final ContractRepositoryPort contractRepositoryPort;

  @Transactional
  public void execute(TerminateContractRequest request) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (!tenantId.equals(request.tenantId())) {
      throw new TenantMismatchException(request.tenantId(), tenantId);
    }

    Contract contract =
        contractRepositoryPort
            .findById(request.contractId())
            .orElseThrow(() -> new ContractNotFoundException(request.contractId()));

    if (contract.getStatus() != ContractStatus.APPROVED) {
      throw new InvalidContractStatusException(contract.getStatus(), ContractStatus.APPROVED);
    }

    String currentUser = SecurityUserContext.getUserIdentifier();
    contract.terminate(contract.getCreatedBy(), currentUser);
    contractRepositoryPort.save(contract);

    log.info(
        "event=LEGAL_CONTRACT_TERMINATE_SUCCESS contractId={} reason={}",
        contract.getContractId(),
        request.reason());
  }
}
