package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.dto.UpdateLegalThresholdRequest;
import com.solveria.core.legal.application.port.AuditLogPort;
import com.solveria.core.legal.application.port.PolicyRuleRepositoryPort;
import com.solveria.core.legal.domain.event.LegalThresholdUpdatedEvent;
import com.solveria.core.legal.domain.exception.LegalThresholdNotFoundException;
import com.solveria.core.legal.domain.exception.PolicyRuleNotFoundException;
import com.solveria.core.legal.domain.exception.ThresholdNotIncreasedException;
import com.solveria.core.legal.domain.model.PolicyRule;
import com.solveria.core.legal.domain.model.vo.LegalThreshold;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateLegalThresholdUseCase {

  private final PolicyRuleRepositoryPort policyRuleRepositoryPort;
  private final AuditLogPort auditLogPort;
  private final Clock clock;
  private final EventOutboxPort eventOutboxPort;

  @Transactional
  public void execute(UpdateLegalThresholdRequest request) {

    PolicyRule policyRule =
        policyRuleRepositoryPort
            .findByPolicyName(request.ruleName())
            .orElseThrow(() -> new PolicyRuleNotFoundException(request.ruleName()));

    BigDecimal previousValue =
        policyRule.getThresholds().stream()
            .max(
                Comparator.comparing(
                    LegalThreshold::effectiveDate, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(LegalThreshold::thresholdValue)
            .orElseThrow(() -> new LegalThresholdNotFoundException(policyRule.getPolicyId()));

    if (request.newValue().compareTo(previousValue) <= 0) {
      throw new ThresholdNotIncreasedException(previousValue, request.newValue());
    }

    LegalThreshold newThreshold = new LegalThreshold(request.newValue(), LocalDate.now(clock));
    policyRule.addThreshold(newThreshold);
    policyRuleRepositoryPort.save(policyRule);

    Instant occurredAt = Instant.now(clock);
    auditLogPort.registerLegalThresholdUpdate(
        policyRule.getPolicyId(),
        policyRule.getPolicyName(),
        previousValue,
        request.newValue(),
        request.userId(),
        occurredAt);
    eventOutboxPort.publish(
        List.of(
            new LegalThresholdUpdatedEvent(
                policyRule.getPolicyId(), request.ruleName(), request.newValue(), occurredAt)));
    log.info(
        "event=LEGAL_POLICY_THRESHOLD_UPDATE_SUCCESS ruleName={} tenantId={}",
        policyRule.getPolicyName(),
        request.tenantId());
  }
}
