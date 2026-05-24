package com.solveria.core.accruals.application.command;

import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.accruals.domain.model.vo.AccrualUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OpenAccrualBalanceCommand(
    UUID relationshipId,
    AccrualBalanceType balanceType,
    AccrualUnit unit,
    BigDecimal currentBalance,
    LocalDate lastAccrualDate,
    String location) {}
