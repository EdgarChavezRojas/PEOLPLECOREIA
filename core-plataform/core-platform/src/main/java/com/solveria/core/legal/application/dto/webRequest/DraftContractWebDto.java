package com.solveria.core.legal.application.dto.webRequest;

import com.solveria.core.legal.application.dto.DraftContractRequest;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DraftContractWebDto(
        @NotNull(message = "El contractId es obligatorio")
        UUID contractId,
        @NotNull(message = "El relationshipId es obligatorio")
        UUID relationshipId,

        @NotNull(message = "El tipo de contrato es obligatorio")
        ContractType contractType,

        @NotNull(message = "La condición de empleo es obligatoria")
        EmploymentCondition employmentCond,

        // Como es UUID, solo usamos @NotNull. El simple hecho de que Spring lo
        // parsee a UUID ya te protege contra inyecciones XSS o formatos raros.
        @NotNull(message = "El projectId no puede ser nulo")
        UUID projectId
) {
    // El DTO sabe cómo convertirse en el Request que el Use Case necesita
    public DraftContractRequest toCommand(UUID tenantId) {
        return new DraftContractRequest(
                null,
                this.relationshipId(),
                this.contractType(),
                this.employmentCond(),
                this.projectId(),
                tenantId
        );
    }

}