package com.solveria.iamservice.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

/**
 * JPA configuration for IAM Service.
 *
 * <p>Registers only the production JPA entities from a core-platform, excluding example/reference
 * classes (e.g. {@code RoleJpaEntityExample}) that contain incomplete JPA mappings and would cause
 * Hibernate initialization errors.
 */
@Configuration
@EntityScan(
        basePackages = {
            "com.solveria.core.accruals.infrastructure.jpa",
            "com.solveria.TimeAndBearings.infrastructure.jpa"
        })
public class JpaConfig {}
