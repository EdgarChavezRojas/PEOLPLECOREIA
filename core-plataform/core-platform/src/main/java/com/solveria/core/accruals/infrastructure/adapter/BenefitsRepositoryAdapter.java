package com.solveria.core.accruals.infrastructure.adapter;

import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.domain.model.BenefitAccrual;
import com.solveria.core.accruals.domain.model.HolidayCalendar;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.model.vo.BenefitType;
import com.solveria.core.accruals.infrastructure.jpa.BenefitAccrualJpa;
import com.solveria.core.accruals.infrastructure.jpa.HolidayCalendarJpa;
import com.solveria.core.accruals.infrastructure.jpa.QuinquenioProvisionJpa;
import com.solveria.core.accruals.infrastructure.mapper.BenefitsMapper;
import com.solveria.core.accruals.infrastructure.repository.BenefitAccrualRepository;
import com.solveria.core.accruals.infrastructure.repository.HolidayCalendarRepository;
import com.solveria.core.accruals.infrastructure.repository.QuinquenioProvisionRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BenefitsRepositoryAdapter implements BenefitsRepositoryPort {

  private final HolidayCalendarRepository holidayCalendarRepository;
  private final QuinquenioProvisionRepository quinquenioProvisionRepository;
  private final BenefitAccrualRepository benefitAccrualRepository;
  private final BenefitsMapper benefitsMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  public HolidayCalendar saveHoliday(HolidayCalendar holiday) {
    HolidayCalendarJpa saved = holidayCalendarRepository.save(benefitsMapper.toJpa(holiday));
    return benefitsMapper.toDomain(saved);
  }

  @Override
  public List<HolidayCalendar> findHolidaysBetween(
      LocalDate startDate, LocalDate endDate, UUID tenantId) {
    if (tenantId == null) {
      String tenantIdStr = SecurityTenantContext.getCurrentTenantId();
      tenantId =
          (tenantIdStr != null && !tenantIdStr.isBlank()) ? UUID.fromString(tenantIdStr) : null;
    }
    return holidayCalendarRepository
        .findByHolidayDateBetweenAndTenantId(startDate, endDate, tenantId)
        .stream()
        .map(benefitsMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional
  public QuinquenioProvision saveQuinquenio(QuinquenioProvision provision) {
    List<DomainEvent> events = provision.pullDomainEvents();
    QuinquenioProvisionJpa savedJpa =
        quinquenioProvisionRepository.save(benefitsMapper.toJpa(provision));
    QuinquenioProvision saved = benefitsMapper.toDomain(savedJpa);

    eventOutboxPort.publish(events);

    return saved;
  }

  @Override
  public Optional<QuinquenioProvision> findQuinquenioByRelationshipId(UUID relationshipId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      return quinquenioProvisionRepository
          .findByRelationshipId(relationshipId)
          .map(benefitsMapper::toDomain);
    }
    return quinquenioProvisionRepository
        .findByRelationshipIdAndTenantId(relationshipId, UUID.fromString(tenantId))
        .map(benefitsMapper::toDomain);
  }

  @Override
  public List<QuinquenioProvision> findAllQuinquenioProvisions() {
    return quinquenioProvisionRepository.findAll().stream().map(benefitsMapper::toDomain).toList();
  }

  @Override
  public BenefitAccrual saveBenefitAccrual(BenefitAccrual accrual) {
    BenefitAccrualJpa saved = benefitAccrualRepository.save(benefitsMapper.toJpa(accrual));
    return benefitsMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public List<BenefitAccrual> saveBenefitAccrualBatch(List<BenefitAccrual> accruals) {
    List<BenefitAccrualJpa> saved =
        benefitAccrualRepository.saveAll(accruals.stream().map(benefitsMapper::toJpa).toList());
    return saved.stream().map(benefitsMapper::toDomain).toList();
  }

  @Override
  public Optional<BenefitAccrual> findBenefitAccrual(
      UUID relationshipId, BenefitType benefitType, int fiscalYear) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      return benefitAccrualRepository
          .findByRelationshipIdAndBenefitTypeAndFiscalYear(relationshipId, benefitType, fiscalYear)
          .map(benefitsMapper::toDomain);
    }
    return benefitAccrualRepository
        .findByRelationshipIdAndBenefitTypeAndFiscalYearAndTenantId(
            relationshipId, benefitType, fiscalYear, UUID.fromString(tenantId))
        .map(benefitsMapper::toDomain);
  }

  @Override
  public List<BenefitAccrual> findAllBenefitAccruals() {
    return benefitAccrualRepository.findAll().stream().map(benefitsMapper::toDomain).toList();
  }
}
