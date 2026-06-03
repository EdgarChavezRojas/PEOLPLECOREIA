package com.solveria.core.accruals.infrastructure.jpa;

import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leave_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTransactionJpa extends BaseEntity {

  @Id
  @Column(name = "transaction_id")
  private UUID transactionId;

  // 1. ELIMINA EL CAMPO 'balanceId' (o coméntalo)
  // @Column(name = "balance_id", nullable = false)
  // private UUID balanceId;
  public UUID getBalanceId() {
    // Si la relación no es nula, obtenemos el ID de ahí
    return this.balance != null ? this.balance.getBalanceId() : null;
  }

  public void setBalanceId(UUID balanceId) {
    // Nota: Como estamos usando @ManyToOne,
    // usualmente no seteamos el ID manualmente,
    // sino que seteamos el objeto completo: transaction.setBalance(accrualBalanceJpa);
    // Pero si quieres mantener este método por compatibilidad:
    if (this.balance == null) {
      this.balance = new AccrualBalanceJpa();
    }
    this.balance.setBalanceId(balanceId);
  }

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId; // Asegúrate de mantener este

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "days_requested", nullable = false)
  private BigDecimal daysRequested;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private LeaveStatus status;

  // 2. CORRIGE LA RELACIÓN PARA QUE SÍ SEA INSERTABLE
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "balance_id", nullable = false) // Quita insertable=false
  private AccrualBalanceJpa balance;
}
