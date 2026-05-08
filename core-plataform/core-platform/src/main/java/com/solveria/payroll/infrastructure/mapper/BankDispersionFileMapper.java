package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.ar.BankDispersionFile;
import com.solveria.payroll.infrastructure.jpa.BankDispersionFileJpa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankDispersionFileMapper {
    BankDispersionFileJpa toEntity(BankDispersionFile domain);
    BankDispersionFile toDomain(BankDispersionFileJpa entity);
}
