package com.solveria.payroll.infrastructure.repository;

import com.solveria.payroll.infrastructure.jpa.PaymentMethodJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: PaymentMethod. */
@Repository
public interface PaymentMethodSpringRepository extends JpaRepository<PaymentMethodJpa, Long> {

  Optional<PaymentMethodJpa> findByPaymentMethodIdAndTenantId(
      UUID paymentMethodId, UUID tenantId);

  List<PaymentMethodJpa> findAllByTenantId(UUID tenantId);

  Optional<PaymentMethodJpa> findByIsDefaultTrueAndTenantId(UUID tenantId);
}
