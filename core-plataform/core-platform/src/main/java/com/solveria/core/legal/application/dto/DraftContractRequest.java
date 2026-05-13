package com.solveria.core.legal.application.dto;

import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record DraftContractRequest(
        UUID contractId,
        UUID relationshipId,
        ContractType contractType,
        EmploymentCondition employmentCond,

        // Blindaje del String: Solo letras, números, guiones y guiones bajos. Nada de etiquetas HTML.
        @NotBlank(message = "El projectId no puede estar vacío")
        @Size(min = 2, max = 50, message = "El projectId debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$", message = "El projectId contiene caracteres no permitidos (riesgo XSS)")
        String projectId,

        // Asumiendo que el tenantId es un UUID en formato String
        @NotBlank(message = "El tenantId es obligatorio")
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Formato de tenantId inválido")
        String tenantId
) {}