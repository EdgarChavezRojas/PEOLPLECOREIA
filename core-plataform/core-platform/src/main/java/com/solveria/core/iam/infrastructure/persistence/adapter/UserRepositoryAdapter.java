package com.solveria.core.iam.infrastructure.persistence.adapter;

import com.solveria.core.iam.application.port.UserRepositoryPort;
import com.solveria.core.iam.domain.model.User;
import com.solveria.core.iam.infrastructure.persistence.entity.RoleJpaEntity;
import com.solveria.core.iam.infrastructure.persistence.mapper.UserJpaMapper;
import com.solveria.core.iam.infrastructure.persistence.repository.RoleJpaRepository;
import com.solveria.core.iam.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final UserJpaRepository userJpaRepository;
  private final RoleJpaRepository roleJpaRepository;
  private final UserJpaMapper mapper;

  public UserRepositoryAdapter(
      UserJpaRepository userJpaRepository,
      RoleJpaRepository roleJpaRepository,
      UserJpaMapper mapper) {
    this.userJpaRepository = userJpaRepository;
    this.roleJpaRepository = roleJpaRepository;
    this.mapper = mapper;
  }

  @Override
  @Cacheable(value = "users", key = "#id", unless = "#result == null")
  public Optional<User> findById(Long id) {
    return userJpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  @Cacheable(value = "usersByEmail", key = "#email", unless = "#result == null")
  public Optional<User> findByEmail(String email) {
    return userJpaRepository.findByEmail(email).map(mapper::toDomain);
  }

  @Override
  @Cacheable(value = "usersByUsername", key = "#username", unless = "#result == null")
  public Optional<User> findByUsername(String username) {
    return userJpaRepository.findByUsername(username).map(mapper::toDomain);
  }

  @Override
  @Caching(
      evict = {
        @CacheEvict(value = "users", key = "#result.id", condition = "#result != null"),
        @CacheEvict(value = "usersByEmail", key = "#result.email", condition = "#result != null"),
        @CacheEvict(
            value = "usersByUsername",
            key = "#result.username",
            condition = "#result != null")
      })
  public User save(User user) {
    // Load RoleJpaEntity entities from IDs
    Set<RoleJpaEntity> roles =
        user.getRoleIds().stream()
            .map(
                roleId ->
                    roleJpaRepository
                        .findById(roleId)
                        .orElseThrow(
                            () -> new IllegalArgumentException("Role not found: " + roleId)))
            .collect(Collectors.toSet());

    var entity = mapper.toEntity(user, roles);
    var saved = userJpaRepository.save(entity);
    return mapper.toDomain(saved);
  }
}
