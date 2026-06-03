package com.solveria.ai.application.dto;

public record EmployeeHandoffRecordDto(
        String relationshipId,
        int unjustifiedAbsences,
        double regularHoursTotal,
        double overtimeHoursTotal) {}
