package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.command.ImputeAnalyticCommand;
import com.solveria.core.financial.application.port.FundingSourceRepositoryPort;
import com.solveria.core.financial.application.port.OrgUnitFinancialDataPort;
import com.solveria.core.financial.domain.model.FundingSource;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter: Resuelve datos financieros de unidades organizativas para imputación analítica.
 *
 * <p>Consulta el {@link FundingSourceRepositoryPort} para localizar la fuente de financiamiento
 * asociada a la unidad organizativa y construye el comando con los datos reales del período.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrgUnitFinancialDataAdapter implements OrgUnitFinancialDataPort {

  private final FundingSourceRepositoryPort fundingSourceRepository;

  @Override
  public ImputeAnalyticCommand buildImputeAnalyticCommand(UUID unitId, UUID newParentId) {
    log.info("event=BUILD_IMPUTE_ANALYTIC_COMMAND unitId={} newParentId={}", unitId, newParentId);

    // Resolver la fuente de financiamiento vinculada a la unidad organizativa.
    // Se busca por unitId como sourceId (en esta versión, el FundingSource se asocia
    // directamente al ID de la unidad organizativa).
    FundingSource fundingSource =
        fundingSourceRepository
            .findById(unitId)
            .orElseThrow(
                () ->
                    new IllegalStateException("FundingSource no encontrado para unitId=" + unitId));

    // Calcular el período del mes en curso para el prorrateo mid-month
    LocalDate transferDate = LocalDate.now();
    LocalDate periodStart = transferDate.withDayOfMonth(1);
    LocalDate periodEnd = transferDate.withDayOfMonth(transferDate.lengthOfMonth());
    // corregir
    return new ImputeAnalyticCommand(
        fundingSource.getSourceId(),
        unitId, // personId: se resuelve a nivel de unidad (el UC maneja la distribución)
        unitId, // oldUnitId: la unidad que fue reasignada
        newParentId, // newUnitId: la nueva unidad padre
        transferDate,
        periodStart,
        periodEnd);
  }
}
