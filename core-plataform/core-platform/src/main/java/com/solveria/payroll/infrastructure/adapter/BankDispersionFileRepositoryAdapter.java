package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.BankDispersionFileRepositoryPort;
import com.solveria.payroll.domain.model.ar.BankDispersionFile;
import com.solveria.payroll.infrastructure.jpa.BankDispersionFileJpa;
import com.solveria.payroll.infrastructure.mapper.BankDispersionFileMapper;
import com.solveria.payroll.infrastructure.repository.BankDispersionFileSpringRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BankDispersionFileRepositoryAdapter implements BankDispersionFileRepositoryPort {

    private final BankDispersionFileSpringRepository repository;
    private final BankDispersionFileMapper mapper;

    public BankDispersionFileRepositoryAdapter(BankDispersionFileSpringRepository repository, BankDispersionFileMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BankDispersionFile save(BankDispersionFile file) {
        BankDispersionFileJpa entity = mapper.toEntity(file);
        BankDispersionFileJpa saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BankDispersionFile> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
