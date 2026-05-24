package com.solveria.TimeAndBearings.infrastructure.mapper;

import com.solveria.TimeAndBearings.domain.model.ar.TimesheetPeriod;
import com.solveria.TimeAndBearings.domain.model.entity.DailyConsolidationSummary;
import com.solveria.TimeAndBearings.domain.model.entity.PayrollHandoffPackage;
import com.solveria.TimeAndBearings.domain.model.enums.ClosureType;
import com.solveria.TimeAndBearings.domain.model.enums.DataQualityFlag;
import com.solveria.TimeAndBearings.domain.model.enums.PeriodStatus;
import com.solveria.TimeAndBearings.domain.model.vo.EmployeeHandoffRecord;
import com.solveria.TimeAndBearings.domain.model.vo.PeriodBoundary;
import com.solveria.TimeAndBearings.infrastructure.jpa.DailyConsolidationSummaryJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.EmployeeHandoffRecordEmbeddable;
import com.solveria.TimeAndBearings.infrastructure.jpa.PayrollHandoffPackageJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.TimesheetPeriodJpa;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Mapper: convierte entre el Aggregate Root de dominio {@link TimesheetPeriod} y las
 * entidades JPA del Aggregate 16.
 *
 * <p><b>Regla de arquitectura:</b> Este mapper vive exclusivamente en la capa de infraestructura.
 * El dominio NUNCA importa clases de este mapper.
 *
 * <p>Gestiona la conversión de:
 *
 * <ul>
 *   <li>{@code TimesheetPeriod} (AR) ↔ {@code TimesheetPeriodJpa}
 *   <li>{@code DailyConsolidationSummary} ↔ {@code DailyConsolidationSummaryJpa}
 *   <li>{@code PayrollHandoffPackage} ↔ {@code PayrollHandoffPackageJpa}
 *   <li>{@code EmployeeHandoffRecord} (VO) ↔ {@code EmployeeHandoffRecordEmbeddable}
 * </ul>
 *
 * <p>Los campos de {@link com.solveria.core.shared.base.BaseEntity} (id, version, createdAt,
 * createdBy, etc.) NO se mapean desde el dominio; son gestionados por JPA/Spring Data Auditing.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TimesheetPeriodMapper {

  // ─────────────────────────────────────────────────────────────────────────
  // Domain → JPA
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Convierte el {@code TimesheetPeriod} AR a su representación JPA.
   *
   * <p>Los enums se mapean a String para persistencia (EnumType.STRING en la JPA). Las entidades
   * hijas se convierten recursivamente.
   *
   * @param domain el aggregate root de dominio
   * @return entidad JPA lista para persistir
   */
  public TimesheetPeriodJpa toJpa(TimesheetPeriod domain) {
    if (domain == null) return null;

    TimesheetPeriodJpa jpa = new TimesheetPeriodJpa();
    jpa.setPeriodId(domain.getPeriodId());
    jpa.setTenantId(domain.getTenantId());
    jpa.setOrgUnitId(domain.getOrgUnitId());

    // PeriodBoundary VO → columnas aplanadas
    PeriodBoundary pb = domain.getPeriodBoundary();
    jpa.setPeriodStart(pb.periodStart());
    jpa.setPeriodEnd(pb.periodEnd());
    jpa.setPeriodType(pb.periodType());
    jpa.setGracePeriodEnd(pb.gracePeriodEnd());

    jpa.setStatus(domain.getStatus().name());
    jpa.setClosedAt(domain.getClosedAt());
    jpa.setClosedBy(domain.getClosedBy());
    jpa.setClosureType(domain.getClosureType() != null ? domain.getClosureType().name() : null);
    jpa.setPayrollEventEmittedAt(domain.getPayrollEventEmittedAt());

    // Entidades hijas: DailyConsolidationSummary
    List<DailyConsolidationSummaryJpa> summaryJpas = new ArrayList<>();
    for (DailyConsolidationSummary s : domain.getDailySummaries()) {
      DailyConsolidationSummaryJpa sJpa = toJpa(s);
      sJpa.setTimesheetPeriod(jpa);
      summaryJpas.add(sJpa);
    }
    jpa.setDailySummaries(summaryJpas);

    // Entidad hija: PayrollHandoffPackage
    if (domain.getHandoffPackage() != null) {
      PayrollHandoffPackageJpa handoffJpa = toJpa(domain.getHandoffPackage());
      handoffJpa.setTimesheetPeriod(jpa);
      jpa.setHandoffPackage(handoffJpa);
    }

    return jpa;
  }

  /**
   * Convierte {@code DailyConsolidationSummary} a su JPA. La referencia al periodo padre ({@code
   * timesheetPeriod}) es asignada por el caller.
   */
  public DailyConsolidationSummaryJpa toJpa(DailyConsolidationSummary domain) {
    if (domain == null) return null;

    DailyConsolidationSummaryJpa jpa = new DailyConsolidationSummaryJpa();
    jpa.setSummaryId(domain.getSummaryId());
    jpa.setWorkDate(domain.getWorkDate());
    jpa.setTotalScheduled(domain.getTotalScheduled());
    jpa.setTotalAttended(domain.getTotalAttended());
    jpa.setTotalNoShows(domain.getTotalNoShows());
    jpa.setTotalExceptionsPending(domain.getTotalExceptionsPending());
    jpa.setTotalRegularHours(domain.getTotalRegularHours());
    jpa.setTotalOvertimeHours(domain.getTotalOvertimeHours());
    jpa.setTotalNightHours(domain.getTotalNightHours());
    jpa.setCalculatedAt(domain.getCalculatedAt());
    return jpa;
  }

  /**
   * Convierte {@code PayrollHandoffPackage} a su JPA. La referencia al periodo padre ({@code
   * timesheetPeriod}) es asignada por el caller.
   */
  public PayrollHandoffPackageJpa toJpa(PayrollHandoffPackage domain) {
    if (domain == null) return null;

    PayrollHandoffPackageJpa jpa = new PayrollHandoffPackageJpa();
    jpa.setHandoffId(domain.getHandoffId());
    jpa.setGeneratedAt(domain.getGeneratedAt());
    jpa.setChecksum(domain.getChecksum());

    List<EmployeeHandoffRecordEmbeddable> embeddables = new ArrayList<>();
    for (EmployeeHandoffRecord r : domain.getEmployeeRecords()) {
      embeddables.add(toEmbeddable(r));
    }
    jpa.setEmployeeRecords(embeddables);
    return jpa;
  }

  /** Convierte el VO {@code EmployeeHandoffRecord} a su Embeddable JPA. */
  public EmployeeHandoffRecordEmbeddable toEmbeddable(EmployeeHandoffRecord vo) {
    if (vo == null) return null;

    EmployeeHandoffRecordEmbeddable e = new EmployeeHandoffRecordEmbeddable();
    e.setRelationshipId(vo.relationshipId());
    e.setRegularHoursTotal(vo.regularHoursTotal());
    e.setOvertimeHoursTotal(vo.overtimeHoursTotal());
    e.setNightHoursTotal(vo.nightHoursTotal());
    e.setHolidayHoursTotal(vo.holidayHoursTotal());
    e.setUnjustifiedAbsences(vo.unjustifiedAbsences());
    e.setRemoteWorkDays(vo.remoteWorkDays());
    e.setDataQualityFlag(vo.dataQualityFlag().name());
    return e;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // JPA → Domain
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Reconstruye el {@code TimesheetPeriod} AR desde su representación JPA.
   *
   * <p>Usa el constructor de reconstrucción del AR, que NO dispara eventos de dominio.
   *
   * @param jpa entidad JPA cargada desde BD
   * @return aggregate root reconstituido
   */
  public TimesheetPeriod toDomain(TimesheetPeriodJpa jpa) {
    if (jpa == null) return null;

    PeriodBoundary boundary =
        new PeriodBoundary(
            jpa.getPeriodStart(), jpa.getPeriodEnd(), jpa.getPeriodType(), jpa.getGracePeriodEnd());

    List<DailyConsolidationSummary> summaries = new ArrayList<>();
    for (DailyConsolidationSummaryJpa sJpa : jpa.getDailySummaries()) {
      summaries.add(toDomain(sJpa));
    }

    PayrollHandoffPackage handoff =
        (jpa.getHandoffPackage() != null) ? toDomain(jpa.getHandoffPackage()) : null;

    return new TimesheetPeriod(
        jpa.getPeriodId(),
        jpa.getTenantId(),
        jpa.getOrgUnitId(),
        boundary,
        PeriodStatus.valueOf(jpa.getStatus()),
        jpa.getClosedAt(),
        jpa.getClosedBy(),
        jpa.getClosureType() != null ? ClosureType.valueOf(jpa.getClosureType()) : null,
        jpa.getPayrollEventEmittedAt(),
        summaries,
        handoff);
  }

  /** Reconstruye {@code DailyConsolidationSummary} desde su JPA. */
  public DailyConsolidationSummary toDomain(DailyConsolidationSummaryJpa jpa) {
    if (jpa == null) return null;

    return new DailyConsolidationSummary(
        jpa.getSummaryId(),
        jpa.getTimesheetPeriod().getPeriodId(),
        jpa.getWorkDate(),
        jpa.getTotalScheduled(),
        jpa.getTotalAttended(),
        jpa.getTotalNoShows(),
        jpa.getTotalExceptionsPending(),
        jpa.getTotalRegularHours(),
        jpa.getTotalOvertimeHours(),
        jpa.getTotalNightHours(),
        jpa.getCalculatedAt());
  }

  /** Reconstruye {@code PayrollHandoffPackage} desde su JPA (con checksum persistido). */
  public PayrollHandoffPackage toDomain(PayrollHandoffPackageJpa jpa) {
    if (jpa == null) return null;

    List<EmployeeHandoffRecord> records = new ArrayList<>();
    for (EmployeeHandoffRecordEmbeddable e : jpa.getEmployeeRecords()) {
      records.add(toDomain(e));
    }

    return new PayrollHandoffPackage(
        jpa.getHandoffId(),
        jpa.getTimesheetPeriod().getPeriodId(),
        jpa.getGeneratedAt(),
        jpa.getChecksum(),
        records);
  }

  /** Reconstruye el VO {@code EmployeeHandoffRecord} desde su Embeddable JPA. */
  public EmployeeHandoffRecord toDomain(EmployeeHandoffRecordEmbeddable e) {
    if (e == null) return null;

    return new EmployeeHandoffRecord(
        e.getRelationshipId(),
        e.getRegularHoursTotal(),
        e.getOvertimeHoursTotal(),
        e.getNightHoursTotal(),
        e.getHolidayHoursTotal(),
        e.getUnjustifiedAbsences(),
        e.getRemoteWorkDays(),
        DataQualityFlag.valueOf(e.getDataQualityFlag()));
  }
}
