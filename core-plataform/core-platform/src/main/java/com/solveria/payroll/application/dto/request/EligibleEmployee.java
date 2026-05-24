package com.solveria.payroll.application.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EligibleEmployee(
    UUID employeeId,
    String fullName,
    String ci,
    String position,
    String department,
    BigDecimal basicSalary,
    int seniorityYears,
    BigDecimal fiscalCredit,
    LocalDate hireDate,
    int workedDays) {}
