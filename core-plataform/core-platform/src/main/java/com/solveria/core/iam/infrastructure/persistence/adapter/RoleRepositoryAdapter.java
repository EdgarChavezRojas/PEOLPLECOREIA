package com.solveria.core.iam.infrastructure.persistence.adapter;

import com.solveria.core.iam.application.port.RoleRepositoryPort;
import com.solveria.core.iam.domain.model.Role;
import com.solveria.core.iam.infrastructure.persistence.mapper.RoleJpaMapper;
import com.solveria.core.iam.infrastructure.persistence.repository.RoleJpaRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {

  private final RoleJpaRepository roleJpaRepository;
  private final RoleJpaMapper mapper;

  public RoleRepositoryAdapter(RoleJpaRepository roleJpaRepository, RoleJpaMapper mapper) {
    this.roleJpaRepository = roleJpaRepository;
    this.mapper = mapper;
  }

  @Override
  @CacheEvict(value = "roles", key = "#result.id", condition = "#result != null")
  public Role save(Role role) {
    var entity = mapper.toEntity(role);
    var saved = roleJpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  @Cacheable(value = "roles", key = "#id", unless = "#result == null")
  public Optional<Role> findById(Long id) {
    return roleJpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Set<Role> findByIds(Set<Long> ids) {
    return roleJpaRepository.findAllById(ids).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toSet());
  }
}
