package com.solveria.iamservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestDataRunner {

    @Bean
    CommandLineRunner printPasswordHash(PasswordEncoder encoder) {
        return args -> {
            System.out.println("HASH password = " + encoder.encode("test123"));
        };
    }
}
