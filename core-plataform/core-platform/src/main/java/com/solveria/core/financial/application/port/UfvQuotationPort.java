package com.solveria.core.financial.application.port;

import com.solveria.core.financial.domain.model.vo.UfvProviderUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Secondary Port: Obtención de cotización UFV del Banco Central de Bolivia.
 *
 * <p>El adaptador debe implementar timeout estricto de 5 segundos. Si el proveedor no responde,
 * lanza {@link UfvProviderUnavailableException}.
 */
public interface UfvQuotationPort {

  /**
   * Obtiene el valor de la UFV para una fecha determinada.
   *
   * @param date Fecha de consulta
   * @return Valor de la UFV en Bolivianos
   * @throws UfvProviderUnavailableException si el servicio del BCB no responde
   */
  BigDecimal getUfvValue(LocalDate date) throws UfvProviderUnavailableException;
}
