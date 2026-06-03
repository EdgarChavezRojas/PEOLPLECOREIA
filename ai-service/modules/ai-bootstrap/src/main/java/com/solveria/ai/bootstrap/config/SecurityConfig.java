package com.solveria.ai.bootstrap.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment env)
            throws Exception {
        boolean dev = Arrays.asList(env.getActiveProfiles()).contains("dev");

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        a -> {
                            a.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll();
                            a.requestMatchers(
                                            "/actuator/health/**",
                                            "/actuator/info/**",
                                            "/api/auth/login",
                                            "/api/auth/google")
                                    .permitAll();
                            if (dev) {
                                a.requestMatchers(
                                                "/v3/api-docs/**",
                                                "/api/v1/ai/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html")
                                        .permitAll();
                                a.anyRequest().permitAll();
                            } else {
                                a.anyRequest().authenticated();
                            }
                        })
                .formLogin(AbstractHttpConfigurer::disable);

        if (!dev) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                    .exceptionHandling(
                            e -> e.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));
        }

        return http.build();
    }
}
