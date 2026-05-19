package com.solveria.core.accruals.application.port;

import java.math.BigDecimal;

public interface AccrualsPolicyCachePort {

  void updateLegalThreshold(String ruleName, BigDecimal newValue);
}

