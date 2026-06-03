package com.solveria.ai.bootstrap.config;

import com.solveria.ai.application.port.out.TenantContextPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/** Tenant context configuration. Dev: fixed 'dev-tenant'. Test: 'test-tenant'. Later: JWT-based. */
@Configuration
public class TenantContextConfig {

    @Bean
    @Profile("dev")
    public TenantContextPort devTenantContextPort(
            @Value("${ai.tenant.dev.tenant-id:e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b}") String tenantId, // <--- CAMBIADO AQUÍ
            @Value("${ai.tenant.dev.principal:juan_perez}") String principal) {
        return simple(tenantId, principal);
    }
    @Bean
    @Profile("test")
    public TenantContextPort testTenantContextPort() {
        return simple("test-tenant", "test-user");
    }

    private static TenantContextPort simple(String tenantId, String principal) {
        return new TenantContextPort() {
            @Override
            public String currentTenantId() {
                return tenantId;
            }

            @Override
            public String principal() {
                return principal;
            }

            @Override
            public List<String> scopes() {
                return List.of();
            }
        };
    }
}
