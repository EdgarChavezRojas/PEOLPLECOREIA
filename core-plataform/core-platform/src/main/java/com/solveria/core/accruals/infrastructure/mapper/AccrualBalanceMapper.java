package com.solveria.core.accruals.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import com.solveria.core.accruals.domain.model.vo.SeniorityMilestone;
import com.solveria.core.accruals.infrastructure.jpa.AccrualBalanceJpa;
import com.solveria.core.accruals.infrastructure.jpa.LeaveTransactionJpa;
import com.solveria.core.accruals.infrastructure.jpa.SeniorityMilestoneJpa;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface AccrualBalanceMapper {

  @Mapping(target = "tenantId", source = "tenantId")
  AccrualBalanceJpa toJpa(AccrualBalance balance);

  // Mapeo limpio: eliminamos los targets que ya no existen como campos en Jpa
  @Mapping(target = "tenantId", source = "tenantId")
  @Mapping(target = "balance", ignore = true)
  LeaveTransactionJpa toJpa(LeaveTransaction transaction);

  SeniorityMilestoneJpa toJpa(SeniorityMilestone milestone);

  default AccrualBalance toDomain(AccrualBalanceJpa jpa) {
    if (jpa == null) return null;

    List<LeaveTransaction> transactions =
        jpa.getLeaveTransactions() == null
            ? List.of()
            : jpa.getLeaveTransactions().stream().map(this::toDomain).toList();

    List<SeniorityMilestone> milestones =
        jpa.getSeniorityMilestones() == null
            ? List.of()
            : jpa.getSeniorityMilestones().stream().map(this::toDomain).toList();

    return new AccrualBalance(
        jpa.getBalanceId(),
        jpa.getRelationshipId(),
        jpa.getBalanceType(),
        jpa.getUnit(),
        jpa.getCurrentBalance(),
        jpa.getInitialBalance(),
        jpa.getDaysAccruedYtd(),
        jpa.getDaysTakenYtd(),
        jpa.getLastAccrualDate(),
        jpa.getTenantId(),
        transactions,
        milestones);
  }

  default LeaveTransaction toDomain(LeaveTransactionJpa jpa) {
    if (jpa == null) return null;
    return new LeaveTransaction(
        jpa.getTransactionId(),
        jpa.getBalanceId(), // Usa el método puente de tu entidad Jpa
        jpa.getTenantId(),
        jpa.getStartDate(),
        jpa.getEndDate(),
        jpa.getDaysRequested(),
        jpa.getStatus());
  }

  default SeniorityMilestone toDomain(SeniorityMilestoneJpa jpa) {
    if (jpa == null) return null;
    return new SeniorityMilestone(
        jpa.getMilestoneId(), jpa.getMonthsCompleted(), jpa.getBaseSmnType());
  }

  @AfterMapping
  default void setBackReferences(@MappingTarget AccrualBalanceJpa jpa, AccrualBalance balance) {
    if (jpa == null) return;

    if (jpa.getLeaveTransactions() != null) {
      for (LeaveTransactionJpa transaction : jpa.getLeaveTransactions()) {
        transaction.setBalance(jpa); // Esto conecta el @ManyToOne y soluciona el NULL en BD
        transaction.setTenantId(jpa.getTenantId());
      }
    }
    if (jpa.getSeniorityMilestones() != null) {
      for (SeniorityMilestoneJpa milestone : jpa.getSeniorityMilestones()) {
        milestone.setBalance(jpa);
      }
    }
  }

  default String toEventPayload(AccrualBalance balance, DomainEvent event) {
    if (balance == null || event == null) return "{}";
    try {
      return new ObjectMapper().writeValueAsString(event);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing AccrualBalance event payload", e);
    }
  }

  // Mapeo limpio para actualizaciones
  @Mapping(target = "balance", ignore = true)
  @Mapping(target = "tenantId", source = "tenantId")
  void updateJpa(LeaveTransaction domain, @MappingTarget LeaveTransactionJpa jpa);
}
