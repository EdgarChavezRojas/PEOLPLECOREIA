package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.entity.PaymentMethod;
import com.solveria.payroll.infrastructure.jpa.PaymentMethodJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMethodMapper {

  @Mapping(target = "isDefault", source = "default")
  PaymentMethodJpa toJpa(PaymentMethod domain);

  @Mapping(target = "isDefault", source = "isDefault")
  PaymentMethod toDomain(PaymentMethodJpa jpa);
}
