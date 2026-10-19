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

### Run the vehicle service

```bash
docker compose up -d postgres
./mvnw -pl vehicle-service spring-boot:run -Dspring-boot.run.profiles=demo
```

The `demo` profile loads sample owners, vehicles, mileage readings and inspections. Without it the database only gets the schema.

Swagger UI: http://localhost:8081/swagger-ui.html

Alternatively, run `TestVehicleServiceApplication` from the IDE: it starts its own PostgreSQL container through Testcontainers, no `docker compose` needed.

### Run the maintenance service

```bash
./mvnw -pl maintenance-service spring-boot:run -Dspring-boot.run.profiles=demo
```

It needs the vehicle service on port 8081 to create bookings. The `demo` profile loads about fourteen months of bookings and priced interventions for the demo fleet, plus upcoming bookings. Notifications go to port 8083; while the notification service is not running they are logged and dropped.

Swagger UI: http://localhost:8082/swagger-ui.html

> If your local database ran the demo data before 19 Oct 2026, recreate it with `docker compose down -v`: the vehicle demo migration changed so that its ids are stable.

## Vehicle service API

| Method | Path | Description |
|---|---|---|
| `GET` `POST` | `/api/owners` | List (paginated) and create owners |
| `GET` `PUT` `DELETE` | `/api/owners/{id}` | Read, update, delete an owner |
| `GET` `POST` | `/api/vehicles` | List with filters (`brand`, `fuelType`, `ownerId`) and register vehicles |
| `GET` `PUT` `DELETE` | `/api/vehicles/{id}` | Vehicle detail with current km and inspection status, update, delete |
| `GET` `POST` | `/api/vehicles/{id}/mileage` | Mileage history (window functions) and new readings |
| `GET` `POST` | `/api/vehicles/{id}/inspections` | Inspection history and new inspections |
| `GET` | `/api/vehicles/inspections/expiring?days=30` | Vehicles with an overdue or upcoming inspection |
| `GET` | `/api/vehicles/stats` | Fleet statistics by fuel type, age and brand |

## Maintenance service API

| Method | Path | Description |
|---|---|---|
| `GET` `POST` | `/api/bookings` | List (filters `vehicleId`, `status`) and book a workshop slot |
| `GET` | `/api/bookings/{id}` | Booking detail |
| `POST` | `/api/bookings/{id}/start` `/cancel` | Lifecycle: `BOOKED → IN_PROGRESS → COMPLETED`, cancellable until completed |
| `POST` | `/api/bookings/{id}/complete` | Record the intervention, priced by plan (`STANDARD`, `WARRANTY`, `FLEET`) |
| `GET` | `/api/interventions` | Interventions, filter by `vehicleId` for a vehicle's history |
| `GET` | `/api/reports/brands/average-cost` | Average cost per brand (`HAVING`) |
| `GET` | `/api/reports/brands/top-interventions` | Most expensive interventions per brand (`RANK() OVER`) |
| `GET` | `/api/reports/monthly-spending?year=` | Spending per month with running total (CTE, `SUM() OVER`) |
| `GET` | `/api/reports/workshop-load` | Booked hours per weekday |

Highlights:

- **Strategy pattern** for pricing: one `CostCalculationStrategy` per plan, chosen by `CostStrategyResolver`.
- **OpenFeign** client to the vehicle service; a custom `ErrorDecoder` turns its 404 into a clean 404 here and any other failure, timeouts included, into 503.
- **Vehicle snapshot**: each booking stores plate, brand and model, because the vehicle table lives in another database. Reports group by these columns.
- **Double-booking protection**: the service checks first for a clear 409, and a PostgreSQL `EXCLUDE` constraint guarantees it under concurrent requests.
- **Notifications** are forwarded after commit and asynchronously; a failure never breaks a booking.

Errors use [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details: 400 for invalid input (with a per-field `errors` map), 404 for unknown resources, 409 for duplicates, 422 for broken business rules, and the maintenance service adds 503 when the vehicle service is unavailable.

## Build and test

```bash
./mvnw verify
```

JaCoCo coverage reports are generated in `<module>/target/site/jacoco/index.html`.
