package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.dto.ContractResponse;
import com.solveria.core.legal.application.dto.DraftContractRequest;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.application.port.ProjectResolutionPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftContractUseCase {

  private final ContractRepositoryPort contractRepositoryPort;
  private final ProjectResolutionPort projectResolutionPort;

  @Transactional
  public ContractResponse execute(DraftContractRequest request) {
    UUID tenantId =
        request.tenantId() != null
            ? request.tenantId()
            : UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    UUID projectId =
        request.projectId() != null
            ? request.projectId()
            : projectResolutionPort.getDefaultProjectIdForTenant(tenantId);

    // revisar porque puede generar fallo
    UUID contractId = request.contractId() != null ? request.contractId() : UUID.randomUUID();
    String createdBy = SecurityUserContext.getUserIdentifier();

    // Unicidad de negocio: evita crear un segundo contrato en DRAFT o ACTIVE para la misma relación
    // Si el cliente no pasa contractId, cada retry genera un UUID nuevo — este guard lo frena
    if (request.contractId() == null
        && contractRepositoryPort.findByRelationshipId(request.relationshipId()).isPresent()) {
      throw new com.solveria.core.shared.exceptions.BusinessRuleViolationException(
          "Ya existe un contrato en curso para la relación '"
              + request.relationshipId()
              + "'. Use el contractId existente para actualizar.");
    }
    EmploymentCondition empCond =
        request.employmentCond() != null
            ? EmploymentCondition.valueOf(request.employmentCond().name())
            : null;
    Contract contract =
        Contract.draft(
            contractId,
            request.relationshipId(),
            request.contractType(),
            empCond,
            projectId,
            tenantId,
            createdBy);

    contractRepositoryPort.save(contract);

    log.info(
        "event=LEGAL_CONTRACT_DRAFT_SUCCESS contractId={} relationshipId={}",
        contractId,
        request.relationshipId());

    return new ContractResponse(
        contract.getContractId(),
        contract.getRelationshipId(),
        contract.getContractType(),
        contract.getEmploymentCond(),
        contract.getStatus(),
        contract.getProjectId(),
        contract.getTenantId(),
        List.of());
  }
}
