package io.github.xydani.fleetcare.vehicle;

import org.springframework.boot.SpringApplication;

/**
 * Runs the service locally against a Testcontainers PostgreSQL instance,
 * without needing docker compose.
 */
public class TestVehicleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(VehicleServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
