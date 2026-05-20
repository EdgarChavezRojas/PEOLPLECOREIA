package com.solveria.core.workforce.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRelationshipRequest(
    UUID personId,
    UUID tenantId, // 100% aislado y seguro
    String relationType,
    LocalDate hireDate,
    String employeeNo,
    String department,
    String jobTitle,
    Integer teachingLoad) {}
