package io.github.xydani.fleetcare.maintenance;

import org.springframework.boot.SpringApplication;

/**
 * Runs the service locally against a Testcontainers PostgreSQL instance,
 * without needing docker compose.
 */
public class TestMaintenanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(MaintenanceServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
