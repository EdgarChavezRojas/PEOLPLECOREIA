package com.solveria.core.financial.domain.model;

import com.solveria.core.financial.domain.event.RcIvaForm110ImportedEvent;
import com.solveria.core.shared.outbox.domain.DomainRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Entity: TaxForm110. Formulario 110 del SIAT para importación de facturas del RC-IVA.
 * verified_credit = total_declared * 13%.
 */
public class TaxForm110 extends DomainRoot {

  private static final BigDecimal IVA_RATE = new BigDecimal("0.13");

  private final UUID formId;
  private final UUID personId;
  private BigDecimal totalDeclared;
  private BigDecimal verifiedCredit;
  private final UUID docId;
  private final YearMonth period;
  private final UUID tenantId;
  private final String createdBy;



  private TaxForm110(
      UUID formId,
      UUID personId,
      BigDecimal totalDeclared,
      BigDecimal verifiedCredit,
      UUID docId,
      YearMonth period,
      UUID tenantId,
      String createdBy) {
    this.formId = formId;
    this.personId = personId;
    this.totalDeclared = totalDeclared;
    this.verifiedCredit = verifiedCredit;
    this.docId = docId;
    this.period = period;
    this.tenantId = tenantId;
    this.createdBy = createdBy;
  }

  /**
   * Factory: importa un nuevo Form 110 con cálculo automático del crédito verificado.
   * verified_credit = totalDeclared * 13%.
   */
  public static TaxForm110 importForm(
      UUID personId,
      BigDecimal totalDeclared,
      UUID docId,
      YearMonth period,
      UUID tenantId,
      String createdBy) {
    if (personId == null) {
      throw new IllegalArgumentException("personId es obligatorio");
    }
    if (totalDeclared == null || totalDeclared.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("totalDeclared no puede ser negativo");
    }
    BigDecimal credit = totalDeclared.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);

    TaxForm110 form =
        new TaxForm110(
            UUID.randomUUID(), personId, totalDeclared, credit, docId, period, tenantId, createdBy);
    form.registerEvent(
        new RcIvaForm110ImportedEvent(
            form.formId, personId, period.toString(), totalDeclared, credit));
    return form;
  }

  /** Factory: rehidrata desde persistencia. */
  public static TaxForm110 rehydrate(
      UUID formId,
      UUID personId,
      BigDecimal totalDeclared,
      BigDecimal verifiedCredit,
      UUID docId,
      YearMonth period,
      UUID tenantId,
      String createdBy) {
    return new TaxForm110(
        formId, personId, totalDeclared, verifiedCredit, docId, period, tenantId, createdBy);
  }

  /** Recalcula el crédito verificado si el total declarado se actualiza. */
  public void recalculateCredit(BigDecimal newTotalDeclared) {
    if (newTotalDeclared == null || newTotalDeclared.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("totalDeclared no puede ser negativo");
    }
    this.totalDeclared = newTotalDeclared;
    this.verifiedCredit = newTotalDeclared.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
  }



  // --- Getters ---

  public UUID getFormId() {
    return formId;
  }

  public UUID getPersonId() {
    return personId;
  }

  public BigDecimal getTotalDeclared() {
    return totalDeclared;
  }

  public BigDecimal getVerifiedCredit() {
    return verifiedCredit;
  }

  public UUID getDocId() {
    return docId;
  }

  public YearMonth getPeriod() {
    return period;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getCreatedBy() {
    return createdBy;
  }
}
