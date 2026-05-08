package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.entity.PaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto Secundario (Outbound): Repositorio de PaymentMethod.
 *
 * <p>Contrato que la capa de infraestructura debe implementar
 * para persistir y consultar métodos de pago.
 */
public interface PaymentMethodRepositoryPort {

    /**
     * Persiste un método de pago.
     */
    void save(PaymentMethod paymentMethod);

    /**
     * Busca un método de pago por su ID y tenantId.
     */
    Optional<PaymentMethod> findById(UUID paymentMethodId, String tenantId);

    /**
     * Lista todos los métodos de pago de un tenant.
     */
    List<PaymentMethod> findAllByTenantId(String tenantId);

    /**
     * Busca el método de pago predeterminado de un tenant.
     */
    Optional<PaymentMethod> findDefault(String tenantId);
}
