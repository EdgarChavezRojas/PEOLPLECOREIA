package com.solveria.core.iam.application.port;

import com.solveria.core.iam.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  User save(User user);
}
