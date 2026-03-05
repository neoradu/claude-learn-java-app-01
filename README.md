# Expense Tracker

Spring Boot 3.5.10 application for managing expenses with server-side rendering (Thymeleaf) and PostgreSQL.

## Stack
- Java 21
- Spring Boot 3.5.10
- Spring MVC + Thymeleaf
- Spring Data JPA (Hibernate)
- PostgreSQL (Docker Compose)

## Features
- Expense CRUD: create, list, edit, delete
- Expense filtering by:
  - date range
  - category
  - title/notes text query
- Managed categories CRUD
- Guard against deleting categories that are in use
- App-wide currency display: `RON`

## Prerequisites
- Java 21
- Maven 3.9+
- Docker (for PostgreSQL)

## Run PostgreSQL
```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with:
- DB: `expenses_db`
- User: `expenses_user`
- Password: `expenses_pass`

## Run the app
```bash
mvn spring-boot:run
```

Application URL:
- http://localhost:8080

## Environment variables
Defaults are configured in `src/main/resources/application.yml`.

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/expenses_db`)
- `DB_USERNAME` (default: `expenses_user`)
- `DB_PASSWORD` (default: `expenses_pass`)
- `PORT` (default: `8080`)

## Test
```bash
mvn test
```

Tests use in-memory H2 with PostgreSQL compatibility mode.

## Schema management note
This project uses:
- `spring.jpa.hibernate.ddl-auto=update`

This is convenient for local development, but for production you should prefer explicit versioned migrations (for example Flyway) to keep schema history deterministic.
