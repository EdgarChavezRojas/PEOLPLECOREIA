package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA Entity: {@code prl_payment_method} — Persistencia del Método de Pago.
 *
 * <p>Extiende {@link BaseEntity} para herencia de {@code tenant_id}, {@code created_at}, {@code
 * created_by}, {@code last_modified_at}, {@code last_modified_by} y {@code version}.
 */
@Entity
@Table(name = "prl_payment_method")
public class PaymentMethodJpa extends BaseEntity {
  @Id
  @Column(
      name = "payment_method_id",
      nullable = false,
      unique = true,
      updatable = false,
      columnDefinition = "UUID")
  private UUID paymentMethodId;

  @Column(name = "channel", nullable = false, length = 30)
  private String channel;

  @Column(name = "is_default", nullable = false)
  private Boolean isDefault;

  @Column(name = "tenant_id")
  private UUID tenantId;

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public PaymentMethodJpa() {
    // JPA
  }

  // ── Getters & Setters ─────────────────────────────────────────────────────

  public UUID getPaymentMethodId() {
    return paymentMethodId;
  }

  public void setPaymentMethodId(UUID paymentMethodId) {
    this.paymentMethodId = paymentMethodId;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }
}
