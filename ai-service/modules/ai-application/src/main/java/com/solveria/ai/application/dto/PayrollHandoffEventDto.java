package com.solveria.ai.application.dto;

import java.util.List;

public record PayrollHandoffEventDto(
        String tenantId,
        String orgUnitId,
        String periodStart,
        String periodEnd,
        List<EmployeeHandoffRecordDto> records) {}
