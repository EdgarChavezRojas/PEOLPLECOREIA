package com.solveria.core.financial.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA Entity: TaxForm110 (Formulario 110 SIAT). Tabla: tax_form_110. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tax_form_110")
public class TaxForm110Jpa extends BaseEntity {
  @Id
  @Column(name = "form_id", nullable = false, unique = true, updatable = false)
  private UUID formId;

  @Column(name = "person_id", nullable = false)
  private UUID personId;

  @Column(name = "total_declared", precision = 15, scale = 2, nullable = false)
  private BigDecimal totalDeclared;

  @Column(name = "verified_credit", precision = 15, scale = 2, nullable = false)
  private BigDecimal verifiedCredit;

  @Column(name = "doc_id")
  private UUID docId;

  @Column(name = "period_year", nullable = false)
  private int periodYear;

  @Column(name = "period_month", nullable = false)
  private int periodMonth;

  @Column(name = "created_by_user")
  private String createdByUser;
}
