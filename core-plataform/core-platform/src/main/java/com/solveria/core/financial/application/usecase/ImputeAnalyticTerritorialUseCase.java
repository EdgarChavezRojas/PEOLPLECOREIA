package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.application.command.ImputeAnalyticCommand;
import com.solveria.core.financial.application.port.FundingSourceRepositoryPort;
import com.solveria.core.financial.domain.model.FundingSource;
import com.solveria.core.financial.domain.model.vo.LaborCostSplit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Imputación Analítica Territorial con prorrateo mid-month. W4: Territorial Analytic
 * Imputation.
 *
 * <p>Cuando un empleado se transfiere entre unidades organizativas a mitad de mes, este caso de uso
 * calcula la distribución proporcional del costo laboral según los días calendario en cada unidad.
 *
 * <p>Si el tenant es Retail/Corporativo y la ubicación es Santa Cruz, también aplica INFOCAL 1%.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImputeAnalyticTerritorialUseCase {

  private final FundingSourceRepositoryPort fundingSourceRepository;

  /** Ejecuta el prorrateo mid-month. Crea dos LaborCostSplit entries que suman 100%. */
  @Transactional
  public void execute(ImputeAnalyticCommand cmd) {
    log.info(
        "event=IMPUTE_ANALYTIC_TERRITORIAL sourceId={} personId={} transferDate={}",
        cmd.sourceId(),
        cmd.personId(),
        cmd.transferDate());

    FundingSource source =
        fundingSourceRepository
            .findById(cmd.sourceId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("FundingSource no encontrado: " + cmd.sourceId()));

    // Calcular días en cada unidad dentro del período
    long totalDays = ChronoUnit.DAYS.between(cmd.periodStart(), cmd.periodEnd()) + 1;
    long daysInOldUnit = ChronoUnit.DAYS.between(cmd.periodStart(), cmd.transferDate());
    long daysInNewUnit = totalDays - daysInOldUnit;

    // Calcular porcentajes proporcionales
    BigDecimal oldPercentage =
        new BigDecimal(daysInOldUnit)
            .multiply(new BigDecimal("100"))
            .divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
    BigDecimal newPercentage = new BigDecimal("100.00").subtract(oldPercentage);

    log.info(
        "event=PRORATION_CALCULATED totalDays={} oldDays={} newDays={} oldPct={} newPct={}",
        totalDays,
        daysInOldUnit,
        daysInNewUnit,
        oldPercentage,
        newPercentage);

    // Crear splits proporcionales que suman 100%
    LocalDate effectiveDate = cmd.transferDate();
    List<LaborCostSplit> proratedSplits =
        List.of(
            LaborCostSplit.create(cmd.oldUnitId(), oldPercentage, effectiveDate),
            LaborCostSplit.create(cmd.newUnitId(), newPercentage, effectiveDate));

    source.adjustCostSplit(proratedSplits);
    fundingSourceRepository.save(source);

    log.info(
        "event=ANALYTIC_IMPUTATION_COMPLETE sourceId={} splits={}",
        cmd.sourceId(),
        proratedSplits.size());
  }
}
