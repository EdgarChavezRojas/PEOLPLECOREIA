package com.solveria.payroll.domain.model.entity;

import com.solveria.payroll.domain.model.vo.PaymentChannel;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Método de Pago — hijo del AR {@code PaymentMethodConfig}.
 *
 * <p>Define el canal de pago y si es el método predeterminado del tenant. Dominio puro.
 */
public class PaymentMethod {

  private final UUID paymentMethodId;
  private final PaymentChannel channel;
  private boolean isDefault;
  private final UUID tenantId;

  /** Constructor de reconstrucción (desde persistencia). */
  public PaymentMethod(
      UUID paymentMethodId, PaymentChannel channel, boolean isDefault, UUID tenantId) {
    this.paymentMethodId = Objects.requireNonNull(paymentMethodId, "paymentMethodId es requerido");
    this.channel = Objects.requireNonNull(channel, "channel es requerido");
    this.isDefault = isDefault;
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
  }

  /** Factory: crea un nuevo método de pago. */
  public static PaymentMethod create(PaymentChannel channel, boolean isDefault, UUID tenantId) {
    return new PaymentMethod(UUID.randomUUID(), channel, isDefault, tenantId);
  }

  /** Marca este método como predeterminado. */
  public void markAsDefault() {
    this.isDefault = true;
  }

  /** Desmarca este método como predeterminado. */
  public void unmarkDefault() {
    this.isDefault = false;
  }

  // ── Getters ──────────────────────────────────────────────────────────────

  public UUID getPaymentMethodId() {
    return paymentMethodId;
  }

  public PaymentChannel getChannel() {
    return channel;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}
