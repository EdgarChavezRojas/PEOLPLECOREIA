package com.solveria.core.financial.domain.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Value Object: Snapshot inmutable del último trimestre indemnizable. Captura el desglose mes a mes
 * del sueldo base y otros conceptos de los últimos 90 días, esencial para imprimir la sección
 * "Liquidación de la Remuneración Promedio Indemnizable" (Tabla II del Ministerio de Trabajo).
 *
 * <p>Estructura: 3 meses × (base + otros) + promedio calculado.
 */
public record IndemnizableTrimSnapshot(
    BigDecimal month1Base,
    BigDecimal month1Others,
    BigDecimal month2Base,
    BigDecimal month2Others,
    BigDecimal month3Base,
    BigDecimal month3Others,
    BigDecimal averageIndemnizable) {

  private static final int TRIM_MONTHS = 3;

  /**
   * Factory: construye el snapshot a partir de las listas de base y otros conceptos de los últimos 3
   * meses.
   *
   * @param monthlyBase Lista de sueldo base por mes (últimos 3 meses, ordenados cronológicamente)
   * @param monthlyOthers Lista de otros conceptos por mes (últimos 3 meses, ordenados
   *     cronológicamente)
   * @return IndemnizableTrimSnapshot inmutable con el promedio calculado
   */
  public static IndemnizableTrimSnapshot build(
      List<BigDecimal> monthlyBase, List<BigDecimal> monthlyOthers) {
    BigDecimal m1b = safeGet(monthlyBase, 0);
    BigDecimal m1o = safeGet(monthlyOthers, 0);
    BigDecimal m2b = safeGet(monthlyBase, 1);
    BigDecimal m2o = safeGet(monthlyOthers, 1);
    BigDecimal m3b = safeGet(monthlyBase, 2);
    BigDecimal m3o = safeGet(monthlyOthers, 2);

    BigDecimal totalMonth1 = m1b.add(m1o);
    BigDecimal totalMonth2 = m2b.add(m2o);
    BigDecimal totalMonth3 = m3b.add(m3o);

    BigDecimal average =
        totalMonth1
            .add(totalMonth2)
            .add(totalMonth3)
            .divide(new BigDecimal(TRIM_MONTHS), 2, RoundingMode.HALF_UP);

    return new IndemnizableTrimSnapshot(m1b, m1o, m2b, m2o, m3b, m3o, average);
  }

  /** Total ganado del mes N (base + otros). */
  public BigDecimal totalMonth(int monthIndex) {
    return switch (monthIndex) {
      case 0 -> month1Base.add(month1Others);
      case 1 -> month2Base.add(month2Others);
      case 2 -> month3Base.add(month3Others);
      default -> BigDecimal.ZERO;
    };
  }

  private static BigDecimal safeGet(List<BigDecimal> list, int index) {
    if (list == null || index >= list.size() || list.get(index) == null) {
      return BigDecimal.ZERO;
    }
    return list.get(index);
  }
}
