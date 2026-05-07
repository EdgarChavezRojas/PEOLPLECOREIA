package com.solveria.core.accruals.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.accruals.domain.event.AccrualEvent;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import com.solveria.core.accruals.domain.model.vo.SeniorityMilestone;
import com.solveria.core.accruals.infrastructure.jpa.AccrualBalanceJpa;
import com.solveria.core.accruals.infrastructure.jpa.LeaveTransactionJpa;
import com.solveria.core.accruals.infrastructure.jpa.SeniorityMilestoneJpa;
import java.util.List;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccrualBalanceMapper {

  AccrualBalanceJpa toJpa(AccrualBalance balance);
  @Mapping(target = "balance", ignore = true)
  @Mapping(target = "balanceId", ignore = true)
  LeaveTransactionJpa toJpa(LeaveTransaction transaction);

  SeniorityMilestoneJpa toJpa(SeniorityMilestone milestone);

  default AccrualBalance toDomain(AccrualBalanceJpa jpa) {
    if (jpa == null) {
      return null;
    }
    List<LeaveTransaction> transactions =
        jpa.getLeaveTransactions() == null
            ? List.of()
            : jpa.getLeaveTransactions().stream().map(this::toDomain).toList();
    List<SeniorityMilestone> milestones =
        jpa.getSeniorityMilestones() == null
            ? List.of()
            : jpa.getSeniorityMilestones().stream().map(this::toDomain).toList();
    return AccrualBalance.builder()
        .balanceId(jpa.getBalanceId())
        .relationshipId(jpa.getRelationshipId())
        .balanceType(jpa.getBalanceType())
        .unit(jpa.getUnit())
        .currentBalance(jpa.getCurrentBalance())
        .initialBalance(jpa.getInitialBalance())
        .daysAccruedYtd(jpa.getDaysAccruedYtd())
        .daysTakenYtd(jpa.getDaysTakenYtd())
        .lastAccrualDate(jpa.getLastAccrualDate())
        .tenantId(jpa.getTenantId())
        .leaveTransactions(transactions)
        .seniorityMilestones(milestones)
        .build();
  }

  default LeaveTransaction toDomain(LeaveTransactionJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return LeaveTransaction.builder()
        .transactionId(jpa.getTransactionId())
        .balanceId(jpa.getBalanceId())
        .startDate(jpa.getStartDate())
        .endDate(jpa.getEndDate())
        .daysRequested(jpa.getDaysRequested())
        .status(jpa.getStatus())
        .build();
  }

  default SeniorityMilestone toDomain(SeniorityMilestoneJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new SeniorityMilestone(
        jpa.getMilestoneId(), jpa.getMonthsCompleted(), jpa.getBaseSmnType());
  }

  @AfterMapping
  default void setBackReferences(@MappingTarget AccrualBalanceJpa jpa, AccrualBalance balance) {
    if (jpa == null) {
      return;
    }
    if (jpa.getLeaveTransactions() != null) {
      for (LeaveTransactionJpa transaction : jpa.getLeaveTransactions()) {
        transaction.setBalance(jpa);
        transaction.setBalanceId(jpa.getBalanceId());
      }
    }
    if (jpa.getSeniorityMilestones() != null) {
      for (SeniorityMilestoneJpa milestone : jpa.getSeniorityMilestones()) {
        milestone.setBalance(jpa);
        milestone.setBalanceId(jpa.getBalanceId());
      }
    }
  }

  default String toEventPayload(AccrualBalance balance, AccrualEvent event) {
    if (balance == null || event == null) {
      return "{}";
    }
    Map<String, Object> payload =
        Map.of(
            "balanceId", balance.getBalanceId(),
            "relationshipId", balance.getRelationshipId(),
            "tenantId", balance.getTenantId(),
            "balanceType",
                balance.getBalanceType() != null ? balance.getBalanceType().name() : null,
            "unit", balance.getUnit() != null ? balance.getUnit().name() : null,
            "eventType", event.type().name());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing AccrualBalance event payload", e);
    }
  }
}
