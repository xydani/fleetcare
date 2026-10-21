package io.github.xydani.fleetcare.notification;

import org.springframework.boot.SpringApplication;

/**
 * Runs the service locally against a Testcontainers MongoDB instance,
 * without needing docker compose.
 */
public class TestNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(NotificationServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
