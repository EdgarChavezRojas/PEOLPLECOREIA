package com.solveria.core.accruals.infrastructure.repository;

import com.solveria.core.accruals.infrastructure.jpa.HolidayCalendarJpa;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendarJpa, UUID> {

  List<HolidayCalendarJpa> findByHolidayDateBetweenAndTenantId(
      LocalDate startDate, LocalDate endDate, UUID tenantId);
}
