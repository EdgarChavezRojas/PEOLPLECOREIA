package com.solveria.core.financial.application.port;

import com.solveria.core.financial.domain.model.TaxForm110;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Repositorio de TaxForm110. */
public interface TaxForm110RepositoryPort {

  Optional<TaxForm110> findById(UUID formId);

  List<TaxForm110> findByPersonIdAndPeriod(UUID personId, YearMonth period, String tenantId);

  void save(TaxForm110 form);
}
