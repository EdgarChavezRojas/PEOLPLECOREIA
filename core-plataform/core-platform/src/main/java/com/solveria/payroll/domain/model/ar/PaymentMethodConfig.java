package com.solveria.payroll.domain.model.ar;

import com.solveria.payroll.domain.model.entity.PaymentMethod;
import com.solveria.payroll.domain.model.vo.PaymentChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root: Configuración de Métodos de Pago.
 *
 * <p>Gestiona la colección de {@link PaymentMethod} para un tenant dado. Invariante: solo puede
 * haber un método marcado como {@code isDefault = true} a la vez.
 *
 * <p><b>Dominio puro:</b> Ninguna anotación de Spring ni JPA.
 */
public class PaymentMethodConfig {

  private final UUID configId;
  private final UUID tenantId;
  private final List<PaymentMethod> methods;

  /** Constructor de reconstrucción (desde persistencia). */
  public PaymentMethodConfig(UUID configId, UUID tenantId, List<PaymentMethod> methods) {
    this.configId = Objects.requireNonNull(configId, "configId es requerido");
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
    this.methods = new ArrayList<>(Objects.requireNonNullElse(methods, List.of()));
  }

  /** Factory: crea una nueva configuración vacía de métodos de pago. */
  public static PaymentMethodConfig create(UUID tenantId) {
    return new PaymentMethodConfig(UUID.randomUUID(), tenantId, new ArrayList<>());
  }

  /**
   * Agrega un nuevo método de pago. Si {@code isDefault} es true, desmarca cualquier otro método
   * existente como default.
   */
  public PaymentMethod addMethod(PaymentChannel channel, boolean isDefault) {
    if (isDefault) {
      methods.forEach(PaymentMethod::unmarkDefault);
    }
    PaymentMethod method = PaymentMethod.create(channel, isDefault, tenantId);
    methods.add(method);
    return method;
  }

  /**
   * Establece un método de pago existente como el predeterminado. Invariante: solo uno puede ser
   * default a la vez.
   */
  public void setDefault(UUID paymentMethodId) {
    PaymentMethod target = findMethodOrThrow(paymentMethodId);
    methods.forEach(PaymentMethod::unmarkDefault);
    target.markAsDefault();
  }

  /** Busca un método de pago por ID. */
  public Optional<PaymentMethod> findMethod(UUID paymentMethodId) {
    return methods.stream().filter(m -> m.getPaymentMethodId().equals(paymentMethodId)).findFirst();
  }

  private PaymentMethod findMethodOrThrow(UUID paymentMethodId) {
    return findMethod(paymentMethodId)
        .orElseThrow(
            () -> new IllegalArgumentException("Método de pago no encontrado: " + paymentMethodId));
  }

  // ── Getters ──────────────────────────────────────────────────────────────

  public UUID getConfigId() {
    return configId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  /**
   * @return vista inmutable de los métodos de pago.
   */
  public List<PaymentMethod> getMethods() {
    return Collections.unmodifiableList(methods);
  }
}
