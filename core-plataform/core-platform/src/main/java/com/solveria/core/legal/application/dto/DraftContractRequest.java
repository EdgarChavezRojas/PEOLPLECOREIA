package com.solveria.core.legal.application.dto;

import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import java.util.UUID;

public record DraftContractRequest(
        UUID contractId,
        UUID relationshipId,
        ContractType contractType,
        EmploymentCondition employmentCond,
        UUID projectId,
        // Asumiendo que el tenantId es un UUID en formato String
        UUID tenantId
) {}