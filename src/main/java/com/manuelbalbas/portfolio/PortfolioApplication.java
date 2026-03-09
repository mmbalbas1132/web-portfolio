package com.manuelbalbas.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot del portafolio.
 * <p>
 * Levanta un servidor embebido (Tomcat) que expone la API REST usada
 * por el formulario de contacto de la página estática.
 */
@SpringBootApplication
public class PortfolioApplication {

    /**
     * Arranca el contexto de Spring Boot y el servidor web embebido.
     *
     * @param args argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}

