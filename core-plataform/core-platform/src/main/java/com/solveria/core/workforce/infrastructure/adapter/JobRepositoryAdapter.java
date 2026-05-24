package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.workforce.application.port.JobRepositoryPort;
import com.solveria.core.workforce.domain.model.Job;
import com.solveria.core.workforce.infrastructure.mapper.JobMapper;
import com.solveria.core.workforce.infrastructure.repository.JobRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobRepositoryAdapter implements JobRepositoryPort {

  private final JobRepository jobRepository;
  private final JobMapper jobMapper;

  @Override
  public Optional<Job> findByJobIdAndTenantId(UUID jobId, UUID tenantId) {
    // Busca en base de datos y usa el mapper para devolver el objeto de Dominio Puro
    return jobRepository.findByJobIdAndTenantId(jobId, tenantId).map(jobMapper::toDomain);
  }

  @Override
  public boolean existsByJobIdAndTenantId(UUID jobId, UUID tenantId) {
    return jobRepository.existsByJobIdAndTenantId(jobId, tenantId);
  }
}
