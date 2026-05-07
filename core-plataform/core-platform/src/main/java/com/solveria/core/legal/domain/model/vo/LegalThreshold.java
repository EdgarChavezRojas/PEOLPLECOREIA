package com.solveria.core.legal.domain.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LegalThreshold(BigDecimal thresholdValue, LocalDate effectiveDate) {}
