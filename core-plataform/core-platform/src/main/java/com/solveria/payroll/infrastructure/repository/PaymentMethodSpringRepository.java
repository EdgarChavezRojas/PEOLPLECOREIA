package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PaymentMethodJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA Repository: PaymentMethod. */
@Repository
public interface PaymentMethodSpringRepository extends JpaRepository<PaymentMethodJpa, Long> {

    Optional<PaymentMethodJpa> findByPaymentMethodIdAndTenantId(UUID paymentMethodId, String tenantId);

    List<PaymentMethodJpa> findAllByTenantId(String tenantId);

    Optional<PaymentMethodJpa> findByIsDefaultTrueAndTenantId(String tenantId);
}
