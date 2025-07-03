package com.turestaurante.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestauranteReservasApplication {

    public static void main(String[] args) {
        // Usa el puerto que Azure proporciona
        String port = System.getenv("PORT");
        if (port != null) {
            System.setProperty("server.port", port);
        }

        SpringApplication.run(RestauranteReservasApplication.class, args);
    }
}