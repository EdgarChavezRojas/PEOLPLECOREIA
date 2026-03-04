package com.solveria.iamservice.config;

import com.solveria.core.iam.infrastructure.persistence.entity.*;
import com.solveria.core.shared.base.BaseEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;

/**
 * JPA configuration for IAM Service.
 *
 * <p>Registers only the production JPA entities from a core-platform, excluding example/reference
 * classes (e.g. {@code RoleJpaEntityExample}) that contain incomplete JPA mappings and would cause
 * Hibernate initialization errors.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.solveria.core.iam.infrastructure.persistence.repository")
public class JpaConfig {

    /**
     * Explicitly lists the managed JPA entity classes so that Hibernate does not pick up example
     * entities from the core-platform entity package.
     */
    @Bean
    PersistenceManagedTypes persistenceManagedTypes() {
        return PersistenceManagedTypes.of(
                BaseEntity.class.getName(),
                RoleJpaEntity.class.getName(),
                PermissionJpaEntity.class.getName(),
                UserJpaEntity.class.getName(),
                ModuleJpaEntity.class.getName(),
                ResourceJpaEntity.class.getName(),
                ActionJpaEntity.class.getName(),
                FieldJpaEntity.class.getName());
    }
}
