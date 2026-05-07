package com.solveria.core.financial.domain.model;

import com.solveria.core.financial.domain.model.vo.ContributionRate;
import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root: SocialSecurityAccount. Administra la afiliación y aportes a la Gestora Pública.
 *
 * <p>Invariante: - Deducción Laboral Exacta: retención fijada estrictamente en 12.71% sobre el
 * Total Ganado.
 */
public class SocialSecurityAccount {

  private final UUID ssaId;
  private final UUID personId;
  private final String gestoraCode;
  private final ContributionRate contributionRate;
  private LocalDate lastContribution;
  private final String tenantId;
  private final String createdBy;

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  private SocialSecurityAccount(
      UUID ssaId,
      UUID personId,
      String gestoraCode,
      ContributionRate contributionRate,
      LocalDate lastContribution,
      String tenantId,
      String createdBy) {
    this.ssaId = ssaId;
    this.personId = personId;
    this.gestoraCode = gestoraCode;
    this.contributionRate = contributionRate;
    this.lastContribution = lastContribution;
    this.tenantId = tenantId;
    this.createdBy = createdBy;
  }

  /** Factory: crea una nueva cuenta de seguridad social con la tasa fija del 12.71%. */
  public static SocialSecurityAccount create(
      UUID personId, String gestoraCode, String tenantId, String createdBy) {
    if (personId == null) {
      throw new IllegalArgumentException("personId es obligatorio");
    }
    if (gestoraCode == null || gestoraCode.isBlank()) {
      throw new IllegalArgumentException("gestoraCode (NUA/CUA) es obligatorio");
    }
    return new SocialSecurityAccount(
        UUID.randomUUID(),
        personId,
        gestoraCode,
        ContributionRate.gestoraDefault(),
        null,
        tenantId,
        createdBy);
  }

  /** Factory: rehidrata desde persistencia. */
  public static SocialSecurityAccount rehydrate(
      UUID ssaId,
      UUID personId,
      String gestoraCode,
      BigDecimal contributionRateValue,
      LocalDate lastContribution,
      String tenantId,
      String createdBy) {
    return new SocialSecurityAccount(
        ssaId,
        personId,
        gestoraCode,
        new ContributionRate(contributionRateValue),
        lastContribution,
        tenantId,
        createdBy);
  }

  /** Calcula la deducción de la Gestora Pública: TG * 12.71%. */
  public BigDecimal calculateGestoraDeduction(BigDecimal totalGanado) {
    if (totalGanado == null || totalGanado.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return totalGanado
        .multiply(this.contributionRate.value())
        .setScale(2, java.math.RoundingMode.HALF_UP);
  }

  /** Actualiza la fecha del último aporte realizado. */
  public void updateLastContribution(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Fecha de contribución no puede ser null");
    }
    this.lastContribution = date;
  }

  public List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = new ArrayList<>(this.domainEvents);
    this.domainEvents.clear();
    return Collections.unmodifiableList(events);
  }

  // --- Getters ---

  public UUID getSsaId() {
    return ssaId;
  }

  public UUID getPersonId() {
    return personId;
  }

  public String getGestoraCode() {
    return gestoraCode;
  }

  public BigDecimal getContributionRateValue() {
    return contributionRate.value();
  }

  public LocalDate getLastContribution() {
    return lastContribution;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getCreatedBy() {
    return createdBy;
  }
}
