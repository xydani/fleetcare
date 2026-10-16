# FleetCare

Backend a microservizi in Spring Boot per gestire una flotta auto: veicoli, manutenzioni, prenotazioni in officina e notifiche. È il backend che avrebbe dovuto avere la mia app Android MyCar Manager.

> 🚧 Lavori in corso.

## Architettura

| Servizio | Porta | Responsabilità | Database |
|---|---|---|---|
| `gateway` | 8080 | Unico punto d'ingresso (Spring Cloud Gateway) | – |
| `vehicle-service` | 8081 | Veicoli, proprietari, chilometri, revisioni | PostgreSQL |
| `maintenance-service` | 8082 | Interventi, prenotazioni, report sui costi | PostgreSQL |
| `notification-service` | 8083 | Eventi e notifiche (es. revisione in scadenza) | MongoDB |

## Stack

Java 21 · Spring Boot · Spring Data JPA · Spring Data MongoDB · Spring Cloud Gateway · OpenFeign · Flyway · MapStruct · springdoc-openapi · JUnit 5 · Mockito · Testcontainers · Docker Compose

## Requisiti

- JDK 21 o superiore (Maven non serve: c'è il wrapper `./mvnw`)
- Docker con Docker Compose

## Sviluppo locale

Avvia PostgreSQL (con `vehicles_db` e `maintenance_db`) e MongoDB:

```bash
docker compose up -d
```

Le credenziali predefinite sono in [`.env.example`](.env.example); per cambiarle copia il file in `.env`.

## Build e test

```bash
./mvnw verify
```

I report di copertura JaCoCo vengono generati in `<modulo>/target/site/jacoco/index.html`.
