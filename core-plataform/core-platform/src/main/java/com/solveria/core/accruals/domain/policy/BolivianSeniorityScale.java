package com.solveria.core.accruals.domain.policy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Escala de Bono de Antigüedad según D.S. 21060 (Bolivia).
 *
 * <p>Define los porcentajes sobre el Salario Mínimo Nacional (SMN) que corresponden a cada tramo de
 * antigüedad laboral.
 *
 * <pre>
 *   2-4  años  →  5%
 *   5-7  años  → 11%
 *   8-10 años  → 18%
 *  11-14 años  → 26%
 *  15-19 años  → 34%
 *  20-24 años  → 42%
 *  25+  años   → 50%
 * </pre>
 */
public final class BolivianSeniorityScale {

  /**
   * NavigableMap donde la key es el año mínimo del tramo y el value es el porcentaje de bono (como
   * BigDecimal). floorEntry(years) retorna el tramo aplicable.
   */
  private static final NavigableMap<Integer, BigDecimal> SCALE;

  static {
    TreeMap<Integer, BigDecimal> map = new TreeMap<>();
    map.put(2, new BigDecimal("5"));
    map.put(5, new BigDecimal("11"));
    map.put(8, new BigDecimal("18"));
    map.put(11, new BigDecimal("26"));
    map.put(15, new BigDecimal("34"));
    map.put(20, new BigDecimal("42"));
    map.put(25, new BigDecimal("50"));
    SCALE = Collections.unmodifiableNavigableMap(map);
  }

  private BolivianSeniorityScale() {}

  /**
   * Retorna el porcentaje de bono de antigüedad para los años dados.
   *
   * @param yearsOfService años de antigüedad (debe ser >= 0)
   * @return porcentaje como BigDecimal (ej. "5", "11"), o BigDecimal.ZERO si < 2 años
   */
  public static BigDecimal bonusPercentageFor(int yearsOfService) {
    if (yearsOfService < 2) {
      return BigDecimal.ZERO;
    }
    var entry = SCALE.floorEntry(yearsOfService);
    return entry != null ? entry.getValue() : BigDecimal.ZERO;
  }

  /**
   * Retorna el multiplicador entero del SMN para el SeniorityMilestoneReachedEvent.
   *
   * <p>Convención: el multiplicador es el porcentaje truncado a entero (5, 11, 18, 26, 34, 42, 50).
   * Retorna 0 si la antigüedad no alcanza el mínimo de 2 años.
   *
   * @param yearsOfService años de antigüedad
   * @return multiplicador entero, o 0 si no aplica
   */
  public static int smMultiplierFor(int yearsOfService) {
    return bonusPercentageFor(yearsOfService).intValue();
  }
}
