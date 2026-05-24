package com.solveria.core.legal.application.dto;

import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import java.util.List;
import java.util.UUID;

public record ContractResponse(
    UUID contractId,
    UUID relationshipId,
    ContractType contractType,
    EmploymentCondition employmentCond,
    ContractStatus status,
    UUID projectId,
    UUID tenantId,
    List<ContractAddendumResponse> addendums) {}
