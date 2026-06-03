package com.solveria.ai.bootstrap;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.solveria.ai")
public class AiServiceApplication {

    public static void main(String[] args) {
        // Carga el archivo .env e inyecta las variables en las propiedades del sistema
        Dotenv dotenv =
                Dotenv.configure()
                        .ignoreIfMissing() // Evita errores si el archivo no existe en producción
                        .load();

        dotenv.entries()
                .forEach(
                        entry -> {
                            // Solo define la propiedad si no existe ya en las variables del
                            // sistema/OS
                            if (System.getProperty(entry.getKey()) == null
                                    && System.getenv(entry.getKey()) == null) {
                                System.setProperty(entry.getKey(), entry.getValue());
                            }
                        });

        SpringApplication.run(AiServiceApplication.class, args);
    }
}
