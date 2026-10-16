# FleetCare

Spring Boot microservice backend for managing a vehicle fleet: vehicles, maintenance, workshop bookings and notifications. It is the backend my Android app MyCar Manager should have had.

> 🚧 Work in progress.

## Architecture

| Service | Port | Responsibility | Database |
|---|---|---|---|
| `gateway` | 8080 | Single entry point (Spring Cloud Gateway) | – |
| `vehicle-service` | 8081 | Vehicles, owners, mileage, inspections | PostgreSQL |
| `maintenance-service` | 8082 | Interventions, bookings, cost reports | PostgreSQL |
| `notification-service` | 8083 | Events and notifications (e.g. inspection due) | MongoDB |

## Stack

Java 21 · Spring Boot · Spring Data JPA · Spring Data MongoDB · Spring Cloud Gateway · OpenFeign · Flyway · MapStruct · springdoc-openapi · JUnit 5 · Mockito · Testcontainers · Docker Compose

## Requirements

- JDK 21 or newer (no Maven install needed: use the `./mvnw` wrapper)
- Docker with Docker Compose

## Local development

Start PostgreSQL (with `vehicles_db` and `maintenance_db`) and MongoDB:

```bash
docker compose up -d
```

Default credentials are in [`.env.example`](.env.example); to change them, copy the file to `.env`.

## Build and test

```bash
./mvnw verify
```

JaCoCo coverage reports are generated in `<module>/target/site/jacoco/index.html`.
