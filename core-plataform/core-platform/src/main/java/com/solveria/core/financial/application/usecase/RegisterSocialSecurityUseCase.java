package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.application.port.HealthProviderRepositoryPort;
import com.solveria.core.financial.application.port.SocialSecurityAccountRepositoryPort;
import com.solveria.core.financial.application.port.SocialSecurityCompliancePort;
import com.solveria.core.financial.domain.model.HealthProvider;
import com.solveria.core.financial.domain.model.SocialSecurityAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Registrar y gestionar cuentas de seguridad social. Implementa
 * SocialSecurityCompliancePort.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterSocialSecurityUseCase implements SocialSecurityCompliancePort {

  private final SocialSecurityAccountRepositoryPort ssaRepository;
  private final HealthProviderRepositoryPort healthProviderRepository;

  @Override
  @Transactional
  public UUID registerSocialSecurityAccount(
      UUID personId, String gestoraCode, String tenantId, String createdBy) {
    log.info("event=REGISTER_SSA personId={} gestoraCode={}", personId, gestoraCode);

    SocialSecurityAccount account =
        SocialSecurityAccount.create(personId, gestoraCode, tenantId, createdBy);
    ssaRepository.save(account);

    log.info("event=SSA_REGISTERED ssaId={}", account.getSsaId());
    return account.getSsaId();
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calculateGestoraDeduction(UUID ssaId, BigDecimal totalGanado) {
    log.info("event=CALCULATE_GESTORA_DEDUCTION ssaId={} totalGanado={}", ssaId, totalGanado);

    SocialSecurityAccount account =
        ssaRepository
            .findById(ssaId)
            .orElseThrow(() -> new IllegalArgumentException("SSA no encontrada: " + ssaId));

    BigDecimal deduction = account.calculateGestoraDeduction(totalGanado);

    log.info("event=GESTORA_DEDUCTION_CALCULATED ssaId={} deduction={}", ssaId, deduction);
    return deduction;
  }

  @Override
  @Transactional
  public void updateLastContribution(UUID ssaId, LocalDate contributionDate) {
    log.info("event=UPDATE_LAST_CONTRIBUTION ssaId={} date={}", ssaId, contributionDate);

    SocialSecurityAccount account =
        ssaRepository
            .findById(ssaId)
            .orElseThrow(() -> new IllegalArgumentException("SSA no encontrada: " + ssaId));

    account.updateLastContribution(contributionDate);
    ssaRepository.save(account);
  }

  @Override
  @Transactional
  public UUID registerHealthProvider(String registrationNo, String tenantId, String createdBy) {
    log.info("event=REGISTER_HEALTH_PROVIDER registrationNo={}", registrationNo);

    HealthProvider provider = HealthProvider.create(registrationNo, tenantId, createdBy);
    healthProviderRepository.save(provider);

    log.info("event=HEALTH_PROVIDER_REGISTERED providerId={}", provider.getProviderId());
    return provider.getProviderId();
  }

  @Override
  @Transactional
  public void suspendHealthProvider(UUID providerId) {
    log.info("event=SUSPEND_HEALTH_PROVIDER providerId={}", providerId);

    HealthProvider provider =
        healthProviderRepository
            .findById(providerId)
            .orElseThrow(
                () -> new IllegalArgumentException("HealthProvider no encontrado: " + providerId));

    provider.suspend();
    healthProviderRepository.save(provider);
  }

  @Override
  @Transactional
  public void activateHealthProvider(UUID providerId) {
    log.info("event=ACTIVATE_HEALTH_PROVIDER providerId={}", providerId);

    HealthProvider provider =
        healthProviderRepository
            .findById(providerId)
            .orElseThrow(
                () -> new IllegalArgumentException("HealthProvider no encontrado: " + providerId));

    provider.activate();
    healthProviderRepository.save(provider);
  }
}
