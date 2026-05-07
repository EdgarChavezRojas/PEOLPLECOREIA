package com.solveria.core.financial.infrastructure.mapper;

import com.solveria.core.financial.domain.model.TaxForm110;
import com.solveria.core.financial.infrastructure.jpa.TaxForm110Jpa;
import java.time.YearMonth;
import org.mapstruct.Mapper;

/** MapStruct Mapper: TaxForm110 Domain ↔ JPA. Maneja la conversión YearMonth ↔ int year/month. */
@Mapper(componentModel = "spring")
public interface TaxForm110Mapper {

  default TaxForm110Jpa toJpa(TaxForm110 form) {
    if (form == null) {
      return null;
    }
    TaxForm110Jpa jpa = new TaxForm110Jpa();
    jpa.setFormId(form.getFormId());
    jpa.setPersonId(form.getPersonId());
    jpa.setTotalDeclared(form.getTotalDeclared());
    jpa.setVerifiedCredit(form.getVerifiedCredit());
    jpa.setDocId(form.getDocId());
    jpa.setPeriodYear(form.getPeriod().getYear());
    jpa.setPeriodMonth(form.getPeriod().getMonthValue());
    jpa.setTenantId(form.getTenantId());
    jpa.setCreatedByUser(form.getCreatedBy());
    return jpa;
  }

  default TaxForm110 toDomain(TaxForm110Jpa jpa) {
    if (jpa == null) {
      return null;
    }
    return TaxForm110.rehydrate(
        jpa.getFormId(),
        jpa.getPersonId(),
        jpa.getTotalDeclared(),
        jpa.getVerifiedCredit(),
        jpa.getDocId(),
        YearMonth.of(jpa.getPeriodYear(), jpa.getPeriodMonth()),
        jpa.getTenantId(),
        jpa.getCreatedByUser());
  }
}
