package com.solveria.core.accruals.infrastructure.listener;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.port.AccrualsPolicyCachePort;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.BenefitAccrual;
import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.accruals.domain.model.vo.AccrualUnit;
import com.solveria.core.accruals.domain.model.vo.BenefitType;
import com.solveria.core.accruals.domain.model.vo.SenioritySpan;
import com.solveria.core.accruals.domain.policy.HolidayPolicy;
import com.solveria.core.experience.domain.event.LeaveRequestedViaEssEvent;
import com.solveria.core.legal.domain.event.LegalThresholdUpdatedEvent;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.event.AcademicProfileRankUpdatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipCreatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipEndedEvent;
import com.solveria.core.workforce.domain.event.RelationshipReactivatedEvent;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccrualsEventListener {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;
  private final BenefitsRepositoryPort benefitsRepository;
  private final RelationshipRepositoryPort relationshipRepository;
  private final ObjectProvider<AccrualsPolicyCachePort> policyCacheProvider;

  @EventListener
  @Transactional
  public void handle(RelationshipCreatedEvent event) {

    UUID relationshipId = event.relationshipId();
    List<AccrualBalance> balances =
        accrualBalanceRepository.findAllByRelationshipId(relationshipId);
    boolean hasVacationBalance =
        balances.stream()
            .anyMatch(balance -> balance.getBalanceType() == AccrualBalanceType.VACATION);

    if (!hasVacationBalance) {
      AccrualBalance balance =
          AccrualBalance.open(
              relationshipId,
              AccrualBalanceType.VACATION,
              AccrualUnit.DAYS,
              BigDecimal.ZERO,
              LocalDate.now(),
              event.tenantId());
      accrualBalanceRepository.save(balance);
    }

    int fiscalYear = LocalDate.now().getYear();
    for (BenefitType benefitType : BenefitType.values()) {
      Optional<BenefitAccrual> existing =
          benefitsRepository.findBenefitAccrual(relationshipId, benefitType, fiscalYear);
      if (existing.isEmpty()) {
        BenefitAccrual accrual =
            BenefitAccrual.open(
                relationshipId, benefitType, fiscalYear, BigDecimal.ZERO, event.tenantId());
        benefitsRepository.saveBenefitAccrual(accrual);
      }
    }

    benefitsRepository
        .findQuinquenioByRelationshipId(relationshipId)
        .orElseGet(
            () ->
                benefitsRepository.saveQuinquenio(
                    QuinquenioProvision.open(relationshipId, BigDecimal.ZERO, event.tenantId())));

    log.info(
        "event=ACCRUALS_RELATIONSHIP_CREATED relationshipId={} tenantId={}",
        relationshipId,
        event.tenantId());
  }

  @EventListener
  @Transactional
  public void handle(RelationshipEndedEvent event) {

    UUID relationshipId = event.relationshipId();
    List<AccrualBalance> balances =
        accrualBalanceRepository.findAllByRelationshipId(relationshipId);
    if (balances.isEmpty()) {
      log.warn(
          "event=ACCRUALS_RELATIONSHIP_ENDED_BALANCE_NOT_FOUND relationshipId={} tenantId={}",
          relationshipId,
          event.tenantId());
      return;
    }

    UUID tenantId = event.tenantId();

    Optional<Relationship> relationship =
        relationshipRepository.findByRelationshipIdAndTenantId(relationshipId, tenantId);
    if (relationship.isEmpty()) {
      log.warn(
          "event=ACCRUALS_RELATIONSHIP_ENDED_RELATIONSHIP_NOT_FOUND relationshipId={} tenantId={}",
          relationshipId,
          event.tenantId());
      return;
    }

    for (AccrualBalance balance : balances) {
      SenioritySpan span = balance.computeSenioritySpan(relationship.get().getHireDate());
      accrualBalanceRepository.save(balance);
      log.info(
          "event=ACCRUALS_RELATIONSHIP_ENDED_SPAN_COMPUTED relationshipId={} years={} months={} days={}",
          relationshipId,
          span.years(),
          span.months(),
          span.days());
    }
  }

  @EventListener
  @Transactional
  public void handle(RelationshipReactivatedEvent event) {
    List<AccrualBalance> balances =
        accrualBalanceRepository.findAllByRelationshipId(event.relationshipId());
    if (balances.isEmpty()) {
      log.warn(
          "event=ACCRUALS_RELATIONSHIP_REACTIVATED_BALANCE_NOT_FOUND relationshipId={}",
          event.relationshipId());
      return;
    }
    // TODO: Agregar lógica de reactivación del reloj de antigüedad en el dominio.
    log.info("event=ACCRUALS_RELATIONSHIP_REACTIVATED relationshipId={}", event.relationshipId());
  }

  @EventListener
  @Transactional
  public void handle(LeaveRequestedViaEssEvent event) {

    List<Relationship> relationships = relationshipRepository.findByPersonId(event.personId());
    Optional<Relationship> activeRelationship =
        relationships.stream()
            .filter(rel -> rel.getCurrentStatus() == RelationshipStatus.ACTIVE)
            .findFirst();

    if (activeRelationship.isEmpty()) {
      log.warn(
          "event=ACCRUALS_LEAVE_REQUEST_RELATIONSHIP_NOT_FOUND personId={} tenantId={}",
          event.personId(),
          event.tenantId());
      return;
    }

    UUID relationshipId = activeRelationship.get().getRelationshipId();
    AccrualBalance balance =
        accrualBalanceRepository.findAllByRelationshipId(relationshipId).stream()
            .filter(bal -> bal.getBalanceType() == AccrualBalanceType.VACATION)
            .findFirst()
            .orElse(null);

    if (balance == null) {
      log.warn(
          "event=ACCRUALS_LEAVE_REQUEST_BALANCE_NOT_FOUND relationshipId={} tenantId={}",
          relationshipId,
          event.tenantId());
      return;
    }

    LocalDate startDate = event.startDate();
    LocalDate endDate = event.endDate();
    List<HolidayCalendar> holidays = benefitsRepository.findHolidaysBetween(startDate, endDate);
    BigDecimal chargeableDays = HolidayPolicy.calculateChargeableDays(startDate, endDate, holidays);

    balance.requestLeave(startDate, endDate, chargeableDays);
    accrualBalanceRepository.save(balance);

    log.info(
        "event=ACCRUALS_LEAVE_REQUESTED_VIA_ESS relationshipId={} personId={}",
        relationshipId,
        event.personId());
  }

  @EventListener
  @Transactional
  public void handle(AcademicProfileRankUpdatedEvent event) {
    List<AccrualBalance> balances =
        accrualBalanceRepository.findAllByRelationshipId(event.relationshipId());
    if (balances.isEmpty()) {
      log.warn(
          "event=ACCRUALS_RANK_UPDATED_BALANCE_NOT_FOUND relationshipId={}",
          event.relationshipId());
      return;
    }
    // TODO: Agregar método de updateRank en dominio.
    log.info(
        "event=ACCRUALS_RANK_UPDATED relationshipId={} newRank={}",
        event.relationshipId(),
        event.newRank());
  }

  @EventListener
  @Transactional
  public void handle(LegalThresholdUpdatedEvent event) {
    AccrualsPolicyCachePort cache = policyCacheProvider.getIfAvailable();
    if (cache == null) {
      log.warn("event=ACCRUALS_LEGAL_THRESHOLD_CACHE_MISSING ruleName={}", event.ruleName());
      return;
    }
    cache.updateLegalThreshold(event.ruleName(), event.newValue());
    log.info(
        "event=ACCRUALS_LEGAL_THRESHOLD_UPDATED ruleName={} newValue={}",
        event.ruleName(),
        event.newValue());
  }
}
