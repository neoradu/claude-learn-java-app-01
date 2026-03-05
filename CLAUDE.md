# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Start the database (required before running the app):**
```bash
docker compose up -d
```

**Build:**
```bash
./mvnw package
```

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Run all tests:**
```bash
./mvnw test
```

**Run a single test class:**
```bash
./mvnw test -Dtest=CategoryControllerTest
```

**Run a single test method:**
```bash
./mvnw test -Dtest=CategoryControllerTest#list_shouldRenderCategoriesPage
```

## Architecture

Standard Spring MVC layered architecture with Thymeleaf server-side rendering.

**Stack:** Spring Boot 3.5.10, Java 21, PostgreSQL (production), H2 (tests), Thymeleaf, Spring Data JPA, Bean Validation.

**Package layout under `com.example.expensetracker`:**
- `domain/` — JPA entities (plain classes with getters/setters, no Lombok)
- `repository/` — Spring Data JPA interfaces
- `service/` — Business logic; throws typed exceptions (`ResourceNotFoundException`, `DuplicateCategoryException`, `CategoryInUseException`)
- `web/` — `@Controller` classes + `GlobalExceptionHandler` (`@ControllerAdvice`); uses form-backing objects in `web/form/`
- `config/` — `DataInitializer` seeds default categories on first startup if the table is empty

**Templates:** `src/main/resources/templates/` using Thymeleaf. Currently has `categories/list.html` and `categories/form.html`.

**Database config:**
- Production: PostgreSQL via `application.yml`; credentials and URL are configurable via env vars `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- Tests: H2 in-memory with `MODE=PostgreSQL`; activated via `@ActiveProfiles("test")` which loads `application-test.yml`
- Schema managed by `hibernate.ddl-auto: update`

**Current state:** Only the `Category` entity is implemented. `GlobalExceptionHandler` currently redirects to `/expenses` (not yet built) on `ResourceNotFoundException`.