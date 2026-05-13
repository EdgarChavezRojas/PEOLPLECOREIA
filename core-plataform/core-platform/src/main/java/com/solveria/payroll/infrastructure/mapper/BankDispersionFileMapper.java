package com.solveria.payroll.infrastructure.mapper;

import com.solveria.payroll.domain.model.ar.BankDispersionFile;
import com.solveria.payroll.infrastructure.jpa.BankDispersionFileJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankDispersionFileMapper {
    @Mapping(target = "id", ignore = true) // Ignores the Long id from BaseEntity
    @Mapping(target = "dispersionFileId", source = "id") // Maps Domain UUID to JPA UUID
    BankDispersionFileJpa toEntity(BankDispersionFile domain);

    @Mapping(target = "id", source = "dispersionFileId")
    BankDispersionFile toDomain(BankDispersionFileJpa entity);
}
