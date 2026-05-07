package com.solveria.core.financial.domain.service;

import com.solveria.core.financial.domain.model.vo.LaborCostSplit;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de Dominio: Valida la invariante de Consistencia del 100% en la distribución del costo
 * laboral (LaborCostSplit).
 *
 * <p>Clase pura sin anotaciones de infraestructura.
 */
public final class LaborCostSplitValidator {

  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

  private LaborCostSplitValidator() {
    // Utility class
  }

  /**
   * Valida que la suma de porcentajes sea exactamente 100%.
   *
   * @throws IllegalStateException si la suma no es 100%
   */
  public static void validateSumIs100(List<LaborCostSplit> splits) {
    if (splits == null || splits.isEmpty()) {
      throw new IllegalArgumentException("La distribución de costos no puede estar vacía");
    }
    BigDecimal sum =
        splits.stream().map(LaborCostSplit::percentage).reduce(BigDecimal.ZERO, BigDecimal::add);

    if (sum.compareTo(ONE_HUNDRED) != 0) {
      throw new IllegalStateException(
          "Invariante Consistencia del 100%: la suma de porcentajes es "
              + sum
              + "%, debe ser 100.00%");
    }
  }

  /** Verifica sin lanzar excepción. */
  public static boolean isSumValid(List<LaborCostSplit> splits) {
    if (splits == null || splits.isEmpty()) {
      return false;
    }
    BigDecimal sum =
        splits.stream().map(LaborCostSplit::percentage).reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.compareTo(ONE_HUNDRED) == 0;
  }
}
