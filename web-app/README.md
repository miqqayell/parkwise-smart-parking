# ParkWise web app

Spring Boot 3.5 (Java 17) web application: real-time parking map, analytics and prediction pages, accounts and subscriptions, sensor monitoring.

```bash
docker compose up -d     # from the repository root
./gradlew bootRun        # http://localhost:8080
./gradlew test           # Cucumber scenarios + unit tests (needs the database)
```

Configuration: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `AI_MODEL_URL` (see `../.env.example`).
PlantUML diagrams of the domain, services and data model are in `src/main/resources/diagrams/`.

See the [main README](../README.md) for the full system.
