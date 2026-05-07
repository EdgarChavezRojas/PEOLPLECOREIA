package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.application.command.AdjustCostSplitCommand;
import com.solveria.core.financial.application.command.ValidateFundingSourceCommand;
import com.solveria.core.financial.application.port.BudgetAllocationPort;
import com.solveria.core.financial.application.port.FundingSourceRepositoryPort;
import com.solveria.core.financial.domain.model.FundingSource;
import com.solveria.core.financial.domain.model.vo.LaborCostSplit;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Validar suficiencia de fondos en un FundingSource. W1: ONG Onboarding Budget Check.
 *
 * <p>Implementa SoD: el aprobador no puede ser el creador del FundingSource.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateFundingSourceUseCase implements BudgetAllocationPort {

  private final FundingSourceRepositoryPort fundingSourceRepository;

  @Override
  @Transactional
  public boolean validateFundingSource(
      UUID sourceId, BigDecimal requiredAmount, String approverUserId) {
    log.info(
        "event=VALIDATE_FUNDING_SOURCE sourceId={} requiredAmount={}", sourceId, requiredAmount);

    ValidateFundingSourceCommand cmd =
        new ValidateFundingSourceCommand(sourceId, requiredAmount, approverUserId);

    FundingSource source =
        fundingSourceRepository
            .findById(cmd.sourceId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("FundingSource no encontrado: " + cmd.sourceId()));

    // SoD: el aprobador no puede ser el creador
    enforceSoD(source.getCreatedBy(), cmd.approverUserId());

    boolean sufficient = source.checkBudgetSufficiency(cmd.requiredAmount());

    // Persistir eventos generados
    fundingSourceRepository.save(source);

    log.info(
        "event=VALIDATE_FUNDING_SOURCE_RESULT sourceId={} sufficient={}", sourceId, sufficient);
    return sufficient;
  }

  @Override
  @Transactional
  public UUID createFundingSource(
      String projectCode, BigDecimal totalBudget, String tenantId, String createdBy) {
    log.info("event=CREATE_FUNDING_SOURCE projectCode={} totalBudget={}", projectCode, totalBudget);

    FundingSource source = FundingSource.create(projectCode, totalBudget, tenantId, createdBy);
    fundingSourceRepository.save(source);

    log.info("event=FUNDING_SOURCE_CREATED sourceId={}", source.getSourceId());
    return source.getSourceId();
  }

  @Override
  @Transactional
  public void adjustCostSplit(
      UUID sourceId, List<AdjustCostSplitCommand.SplitEntry> splits, String approverUserId) {
    log.info("event=ADJUST_COST_SPLIT sourceId={} splitCount={}", sourceId, splits.size());

    FundingSource source =
        fundingSourceRepository
            .findById(sourceId)
            .orElseThrow(
                () -> new IllegalArgumentException("FundingSource no encontrado: " + sourceId));

    // SoD: el aprobador no puede ser el creador
    enforceSoD(source.getCreatedBy(), approverUserId);

    List<LaborCostSplit> domainSplits =
        splits.stream()
            .map(e -> LaborCostSplit.create(e.unitId(), e.percentage(), e.effectiveDate()))
            .toList();

    source.adjustCostSplit(domainSplits);
    fundingSourceRepository.save(source);

    log.info("event=COST_SPLIT_ADJUSTED sourceId={}", sourceId);
  }

  @Override
  @Transactional
  public void allocateBudget(UUID sourceId, BigDecimal amount) {
    log.info("event=ALLOCATE_BUDGET sourceId={} amount={}", sourceId, amount);

    FundingSource source =
        fundingSourceRepository
            .findById(sourceId)
            .orElseThrow(
                () -> new IllegalArgumentException("FundingSource no encontrado: " + sourceId));

    source.allocateBudget(amount);
    fundingSourceRepository.save(source);

    log.info(
        "event=BUDGET_ALLOCATED sourceId={} remainingBudget={}",
        sourceId,
        source.getAvailableBudget());
  }

  /** Segregación de Funciones (SoD): el aprobador no puede ser el creador del recurso. */
  private void enforceSoD(String creatorId, String approverId) {
    if (creatorId != null && creatorId.equals(approverId)) {
      throw new IllegalStateException(
          "Segregación de Funciones (SoD): el aprobador ("
              + approverId
              + ") no puede ser el mismo que el creador ("
              + creatorId
              + ")");
    }
  }
}
