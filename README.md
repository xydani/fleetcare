# FleetCare

[![CI](https://github.com/xydani/fleetcare/actions/workflows/ci.yml/badge.svg)](https://github.com/xydani/fleetcare/actions/workflows/ci.yml)
![Coverage](https://img.shields.io/badge/coverage-lines%20%E2%89%A5%2085%25%20%C2%B7%20branches%20%E2%89%A5%2080%25-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6db33f)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

Spring Boot microservice backend for managing a vehicle fleet: vehicles, maintenance, workshop bookings and notifications. It is the backend my Android app MyCar Manager should have had.

> 🚧 Work in progress.

## Architecture

| Service | Port (local) | Responsibility | Database |
|---|---|---|---|
| `gateway` | 8080 | Single entry point (Spring Cloud Gateway) | – |
| `vehicle-service` | 8081 | Vehicles, owners, mileage, inspections | PostgreSQL |
| `maintenance-service` | 8082 | Interventions, bookings, cost reports | PostgreSQL |
| `notification-service` | 8083 | Events and notifications (e.g. inspection due) | MongoDB |

In Docker Compose only the gateway is published; the services talk to each other over the compose network.

## Stack

Java 21 · Spring Boot · Spring Data JPA · Spring Data MongoDB · Spring Cloud Gateway · OpenFeign · Flyway · MapStruct · springdoc-openapi · JUnit 5 · Mockito · Testcontainers · Docker Compose

## Quick start

All you need is Docker with Docker Compose:

```bash
docker compose up --build --wait
```

The first build takes a few minutes; then the whole system (two databases, three services, the gateway) is up with demo data. Everything is reached through the gateway on port 8080:

- **Swagger UI**: http://localhost:8080/swagger-ui.html (pick the service in the drop-down at the top)
- Stop with `docker compose down`; add `--volumes` to also delete the data.

Settings such as ports and the demo profile are in [`.env.example`](.env.example); copy it to `.env` to change them.

### A quick tour

In the Swagger UI, or with any HTTP client against `http://localhost:8080`:

1. `GET /api/vehicles/1`: a demo vehicle with its current mileage and inspection status.
2. `POST /api/bookings` with `{"vehicleId": 1, "serviceType": "BRAKES", "startsAt": "<a future weekday>T09:00:00"}`.
3. `POST /api/bookings/{id}/start`, then `POST /api/bookings/{id}/complete` with `{"description": "Brake pads", "partsCost": 120, "labourHours": 1.5, "pricingPlan": "FLEET"}`: the fleet plan applies a 15% discount.
4. `GET /api/notifications?unread=true`: the booking and the completed work became notifications.
5. `POST /api/reminders/inspections`: creates reminders for overdue and upcoming inspections; run it twice and the second time creates none.
6. `GET /api/reports/monthly-spending` and `GET /api/vehicles/stats`: reports over the demo data.

## Local development

Requirements: JDK 21 or newer (no Maven install needed: use the `./mvnw` wrapper) and Docker.

Start only PostgreSQL (with `vehicles_db` and `maintenance_db`) and MongoDB, then run the services you work on from the IDE or with Maven:

```bash
docker compose up -d postgres mongo
```

Default credentials are in [`.env.example`](.env.example); to change them, copy the file to `.env`.

### Run the vehicle service

```bash
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

### Run the notification service

```bash
./mvnw -pl notification-service spring-boot:run
```

Every day at 07:00 it asks the vehicle service for overdue and upcoming inspections and creates one reminder per vehicle and due date. Change the schedule with `REMINDERS_CRON` (`-` disables it), or run the check at once with `POST /api/reminders/inspections`.

Swagger UI: http://localhost:8083/swagger-ui.html

### Run the gateway

```bash
./mvnw -pl gateway spring-boot:run
```

The single entry point on port 8080: every `/api/**` call goes through it to the right service.

- **Swagger UI for the whole API**: http://localhost:8080/swagger-ui.html, with a drop-down to pick the service.
- **Correlation id**: every response carries an `X-Correlation-Id` header (the client's own, if valid, or a new UUID), which is also sent to the service and written in the gateway's request log with status and duration.
- **CORS** for browser front ends, configured once here (`CORS_ALLOWED_ORIGINS`).
- Service URLs come from `VEHICLE_SERVICE_URL`, `MAINTENANCE_SERVICE_URL` and `NOTIFICATION_SERVICE_URL` (localhost by default). Routes are static: with three services a service registry such as Eureka would add moving parts without benefit.


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

## Notification service API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/events` | Report an event (`BOOKING_CONFIRMED`, `MAINTENANCE_COMPLETED`, `INSPECTION_DUE`); repeats are detected |
| `GET` | `/api/events/stats?days=30` | Events per day and type (aggregation pipeline) |
| `GET` | `/api/notifications` | Newest first, filters `unread=true` and `vehicleId` |
| `GET` | `/api/notifications/unread-count` | Number of unread notifications |
| `PATCH` | `/api/notifications/{id}/read` | Mark as read |
| `POST` | `/api/reminders/inspections` | Run the daily inspection check now |

Highlights:

- **Factory pattern**: `NotificationFactory` turns an event type and its free-form payload into a notification, validating the fields each type needs (422 otherwise).
- **MongoDB** fits here: payloads differ per event type and are written once. Events expire after 90 days through a **TTL index**.
- **Idempotent events**: each notification has a deduplication key under a unique index, so retries and the daily job never create duplicates.
- **Aggregation pipeline** (`$match`, `$project`, `$group`, `$sort`) for the statistics, grouped by local day.

Errors use [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details: 400 for invalid input (with a per-field `errors` map), 404 for unknown resources, 409 for duplicates, 422 for broken business rules, and the maintenance service adds 503 when the vehicle service is unavailable.

## Build and test

```bash
./mvnw verify
```

Runs every test (unit, web slice, Testcontainers with real PostgreSQL and MongoDB, WireMock for calls between services) and checks coverage: the build fails if a module drops below 85% of lines or 80% of branches. Docker must be running for the Testcontainers tests.

The coverage of all modules together is in `coverage-report/target/site/jacoco-aggregate/index.html`. On GitHub, every CI run shows a coverage table in its summary and keeps the HTML report as an artifact.
