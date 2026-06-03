package com.solveria.core.accruals.infrastructure.jpa;

import com.solveria.core.accruals.domain.model.vo.HolidayScope;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "holiday_calendar",
    indexes = {
      @Index(name = "idx_holiday_calendar_date", columnList = "holiday_date"),
      @Index(name = "idx_holiday_calendar_tenant", columnList = "tenant_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayCalendarJpa extends BaseEntity {

  @Id
  @Column(name = "holiday_id")
  private UUID holidayId;

  @Column(name = "holiday_date", nullable = false)
  private LocalDate holidayDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope", nullable = false)
  private HolidayScope scope;
}
