package com.solveria.core.legal.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryTermsEmbeddable {

  @Column(name = "basic_salary", precision = 15, scale = 2)
  private BigDecimal basicSalary;

  @Column(name = "total_earned_proj", precision = 15, scale = 2)
  private BigDecimal totalEarnedProj;

  @Column(name = "net_salary_proj", precision = 15, scale = 2)
  private BigDecimal netSalaryProj;

  @Column(name = "currency", length = 3)
  private String currency;
}
