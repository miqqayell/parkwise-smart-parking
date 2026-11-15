# ParkWise

A Spring Boot application for smart parking assistance. It manages parking areas and spots, ingests sensor data, provides predictions on parking availability, and offers subscription-based features with a simple web UI (Thymeleaf) and REST/MVC controllers.

> NOTE: This README was created on 2025-12-15 based on the current code. If something looks off, please open an issue or update this document. Unknowns are marked as TODO.

ParkWise is a smart parking system made of **multiple services**:

- **DataCollector** ingests sensor readings and stores them in PostgreSQL.
- **Analytics Service (Python/Flask)** queries PostgreSQL and generates charts + stats for dashboards.
- **Web App (Spring Boot + Thymeleaf)** serves the UI and calls the Analytics API.

## System Architecture

PostgreSQL (5432)  
↓  
DataCollector (ingestion service)  
↓  
Python Analytics Service (Flask, 5000)
- Queries database
- Generates charts (Matplotlib/Seaborn)
- Returns JSON + base64 PNG images  
  ↓  
  Spring Boot Web App (8080)
- Calls Analytics API
- Renders dashboards via Thymeleaf

## Components

### 1) Web App (Spring Boot)
- Spring Boot 3.x (Java 17)
- MVC views with Thymeleaf (`src/main/resources/templates`)
- Spring Data JPA + PostgreSQL
- REST/MVC controllers for pages like home, auth, dashboard, parking, predictions, analytics, subscriptions, users (exact list depends on current code)

### 2) DataCollector (Ingestion)
Purpose: **collect sensor events** and write them into PostgreSQL.

Typical responsibilities:
- Normalize raw sensor payloads (distance/metal detection/occupied)
- Attach readings to known sensors/spots/areas
- Insert into `sensor_readings` (and related tables if needed)

### 3) Analytics Service (Python Flask)
Runs on port **5000** and exposes analytics endpoints for the Web App.

**Endpoints (current implementation):**
- `GET /` → service info (recommended to include)
- `GET /health`
- `GET /api/analytics/occupancy-over-time`
- `GET /api/analytics/spot-heatmap`
- `GET /api/analytics/distance-scatter`
- `GET /api/analytics/peak-hours`
- `GET /api/analytics/stats`

Responses typically include:
- `image`: `data:image/png;base64,...` (Matplotlib output)
- `data`: JSON records used to build the chart

## Tech Stack
- Language: Java 17
- Framework: Spring Boot 3.5.6
- Build tool / package manager: Gradle (Kotlin DSL) with wrapper (`gradlew`, `gradlew.bat`)
- View layer: Thymeleaf
- Persistence: Spring Data JPA (Hibernate)
- Database: PostgreSQL (JDBC driver)
- Validation: Jakarta Bean Validation
- Security: Spring Security (current config permits all; CSRF disabled)
- Testing: JUnit Platform via `spring-boot-starter-test`


### Analytics Service
- Python (recommended: **3.12** for easiest dependency compatibility; **3.13** works with modern versions)
- Flask + flask-cors
- psycopg / psycopg2 (PostgreSQL driver)
- Matplotlib, Seaborn, Pandas, NumPy

Key packages:
- `be.kdg.parkwise.controller` — Controllers for home, authentication, dashboard, parking, predictions, analytics, subscriptions, upgrade, and users.
- `be.kdg.parkwise.service` — Service interfaces and implementations (parking, prediction/AI, sensors, subscriptions, users).
- `be.kdg.parkwise.domain` — Entities and value objects.
- `be.kdg.parkwise.repository` — Spring Data repositories and custom implementations.
- `be.kdg.parkwise.config` — Application configuration (security, DB placeholder, database seeding via `AppRunner`).

Database seeding: On startup, `AppRunner.seedDatabase` creates several campus parking areas and one parking spot per area if the database has no spots yet.


## Requirements
- JDK 17
- PostgreSQL 13+ (recommended)
- Internet access to download dependencies (Maven Central)
- Optional: PlantUML viewer for diagrams under `src/main/resources/diagrams`

## Configuration

### 1) PostgreSQL
You need a PostgreSQL database reachable by:
- the DataCollector
- the Analytics Service
- the Spring Boot Web App

Your analytics queries expect at least these tables/relations:
- `sensor_readings` with columns like `reading_time`, `occupied`, `distance_mm`, `metal_detected`, `sensor_id`
- `sensors` linking to `spot_id`
- `parking_spots` linking to `area_id`
- `parking_areas`

### 2) Analytics Service environment variables
Do **not** hardcode DB credentials in source.

Recommended environment variables:
- `PW_DB_HOST`
- `PW_DB_PORT`
- `PW_DB_NAME`
- `PW_DB_USER`
- `PW_DB_PASSWORD`

### 3) Spring Boot environment variables
Use Spring Boot standard variables:
- `SPRING_DATASOURCE_URL` — e.g. `jdbc:postgresql://localhost:5432/parkwise`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT` — defaults to 8080

Recommended for integration:
- `PARKWISE_ANALYTICS_BASEURL` — e.g. `http://localhost:5000`

## Setup & Run

## Step 1
1. Clone the repository.
2. Ensure PostgreSQL is running and create a database (e.g., `iotdb`).
3. Set environment variables for datasource as shown above, or edit a local configuration (e.g., `application-local.yml`) and activate that profile.
4. Build and run:
   - Run with Gradle wrapper:
     - Windows: `./gradlew.bat bootRun`
     - macOS/Linux: `./gradlew bootRun`
   - Or build a JAR and run:
     - `./gradlew build`
     - `java -jar build/libs/ParkWise-0.0.1-SNAPSHOT.jar`

## Entry Point
- Main class: `be.kdg.parkwise.ParkWiseApplication`
- Starts a Spring Boot application on port `8080` by default.

## Endpoints & UI
- MVC pages (templates): `home`, `about`, `prediction`, `analytics`, `subscriptions`, `upgrade`, `user` (see `src/main/resources/templates/*.html`).
- Controllers exist for analytics, predictions, subscriptions, dashboard, parking, user, and authentication.

TODO: Document REST endpoints and mappings for each controller (paths, methods, request/response models).

## Security
- `SecurityConfig` currently:
  - Permits all requests (`.anyRequest().permitAll()`)
  - Disables form login, logout, and CSRF
- `BCryptPasswordEncoder` bean is provided.

TODO: Review and harden security for production (authentication, authorization rules, CSRF for state-changing requests, session management).

## Authentication & User Accounts
- Signup: handled by `AuthController#doSignup`, which calls `UserService#registerUser`.
  - Passwords are hashed using `BCryptPasswordEncoder` before being persisted.
  - Duplicate emails are rejected.
- Login: handled by `AuthController#doLogin`, which calls `UserService#authenticateUser`.
  - On successful authentication, a lightweight `UserSessionView` is placed in the HTTP session (attribute name: `currentUser`).
  - The `User` entity persists a `last_login` timestamp on successful login for auditing (`User.lastLogin`).

How to verify login persistence:
- Check the `users` table after logging in; the `last_login` column should be updated for the user.
- Example SQL:
  ```sql
  SELECT id, email, created_at, last_login FROM users WHERE email = 'you@example.com';
  ```

Console/SQL logging tips:
- To see SQL statements in the console during development, you can run with the following environment variables:
  - `SPRING_JPA_SHOW_SQL=true`
  - `SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true`
- Or add the following logger to see parameter values (logback/log4j2 config dependent):
  - Logger name: `org.hibernate.type.descriptor.sql`
  - Level: `TRACE`

Session usage in templates/controllers:
- Controllers often read the current user with:
  ```java
  AuthController.UserSessionView currentUser = (AuthController.UserSessionView) session.getAttribute("currentUser");
  ```
- A `@ControllerAdvice` (`UserSessionAdvice`) exposes the current user to all views if present.

## Scripts & Tasks
Common Gradle wrapper commands:
- `./gradlew tasks` — list tasks
- `./gradlew bootRun` — run the app
- `./gradlew build` — build the JAR
- `./gradlew test` — run tests
- `./gradlew clean` — clean build outputs

Windows equivalents use `./gradlew.bat`.

## Tests
- Framework: JUnit with Spring Boot Test
- Run all tests: `./gradlew test`
- Example test class: `src/test/java/be/kdg/parkwise/ParkWiseApplicationTests.java`

TODO: Add more unit/integration tests for services and controllers.

## Project Structure
```
.
├─ build.gradle.kts
├─ settings.gradle.kts
├─ gradlew / gradlew.bat
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ be/kdg/parkwise
│  │  │     ├─ ParkWiseApplication.java
│  │  │     ├─ config/ (SecurityConfig, AppRunner, ...)
│  │  │     ├─ controller/ (HomeController, AuthController, ...)
│  │  │     ├─ domain/ (entities, value objects)
│  │  │     ├─ dto/
│  │  │     ├─ integration/ (GSMReader, impl)
│  │  │     ├─ mapper/
│  │  │     ├─ repository/ (Spring Data + impl)
│  │  │     └─ service/ (+ impl)
│  │  └─ resources
│  │     ├─ application.yml / application.properties
│  │     ├─ diagrams/ (*.puml)
│  │     ├─ static/ (css, images)
│  │     └─ templates/ (*.html)
│  └─ test
│     └─ java/be/kdg/parkwise/ParkWiseApplicationTests.java
└─ README.md
```

## Diagrams
PlantUML diagrams are in `src/main/resources/diagrams`:
- `controller_config.puml`, `domain.puml`, `packageOverview.puml`, `parkwise-fullDiagram.puml`, `repositories.puml`, `sequenceDiagram.puml`, `services.puml`

## Development Notes
- `developmentOnly("org.springframework.boot:spring-boot-devtools")` is included for hot reload in development.
- The app seeds initial data via `AppRunner` when there are no parking spots in the database.
- Prediction service has an implementation `RandomForestAIModel` placeholder — review model training/inference integration. TODO: Document data sources and model lifecycle.

## License
TODO: Add license information (e.g., MIT, Apache-2.0). If you intend the project to be open source, include a `LICENSE` file in the repository.

## Contributing
Contributions are welcome. Please open an issue to propose changes, especially regarding configuration, security, and endpoint documentation.
