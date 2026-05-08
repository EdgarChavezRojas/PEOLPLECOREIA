package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PaymentMethodRepositoryPort;
import com.solveria.payroll.domain.model.entity.PaymentMethod;
import com.solveria.payroll.infrastructure.jpa.PaymentMethodJpa;
import com.solveria.payroll.infrastructure.mapper.PaymentMethodMapper;
import com.solveria.payroll.infrastructure.repository.PaymentMethodSpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: Implementación del {@link PaymentMethodRepositoryPort}.
 *
 * <p>Persiste y consulta métodos de pago usando Spring Data JPA
 * y el mapper bidireccional Domain ↔ JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepositoryPort {

    private final PaymentMethodSpringRepository springRepository;
    private final PaymentMethodMapper mapper;

    @Override
    @Transactional
    public void save(PaymentMethod paymentMethod) {
        log.info("event=PRL_PAYMENT_METHOD_SAVE paymentMethodId={} tenantId={}",
                paymentMethod.getPaymentMethodId(), paymentMethod.getTenantId());
        PaymentMethodJpa jpa = mapper.toJpa(paymentMethod);
        springRepository.save(jpa);
    }

    @Override
    public Optional<PaymentMethod> findById(UUID paymentMethodId, String tenantId) {
        return springRepository.findByPaymentMethodIdAndTenantId(paymentMethodId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public List<PaymentMethod> findAllByTenantId(String tenantId) {
        return springRepository.findAllByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PaymentMethod> findDefault(String tenantId) {
        return springRepository.findByIsDefaultTrueAndTenantId(tenantId)
                .map(mapper::toDomain);
    }
}
