# SolarShare - Community Rooftop Solar Usage Tracker

A Spring Boot REST API for a housing community that shares one rooftop solar installation.
It records daily solar generation, each household's fixed share of that generation, each household's
consumption, the energy each household exports, and a monthly net-usage summary.
**All derived values (generation share, export, monthly totals, net usage) are calculated by the server.**
Clients only send raw inputs.

## 1. Technologies

| Area | Choice |
|---|---|
| Language / build | Java 17+ (works on 21), Maven |
| Framework | Spring Boot 3.3.5 (Web, Data JPA, Validation) |
| Database | PostgreSQL (docker-compose provided); H2 in-memory for tests |
| API docs | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5, Mockito, MockMvc, `@SpringBootTest` |

## 2. Architecture

Layered: `controller` (HTTP, validation) -> `service` (business rules, transactions) -> `repository` (Spring Data JPA) -> `entity`.
DTOs (Java records) are used for every request and response, so JPA entities are never serialised
(no lazy-loading or infinite-recursion problems). Errors are converted to JSON by a `@RestControllerAdvice`.

## 3. Database design and relationships

```
Installation 1 --- * GenerationLog     (unique: installation + generation_date)
Installation 1 --- * Household
Household    1 --- * ConsumptionLog    (unique: household + consumption_date)
```

Relationships are unidirectional `@ManyToOne` (`@JoinColumn`) from the child side only.
"Installation has many logs/households" is queried through repositories, which avoids bidirectional
mapping and serialisation problems. Schema is created automatically (`spring.jpa.hibernate.ddl-auto=update`).

| Table | Columns |
|---|---|
| installations | id, name, location, total_capacity (kW), created_at |
| generation_logs | id, installation_id, generation_date, units_generated (kWh), created_at |
| households | id, installation_id, household_name, owner_name, allocation_ratio (decimal 0-1), created_at |
| consumption_logs | id, household_id, consumption_date, units_consumed, units_exported (server-calculated), created_at |

`allocationRatio` is a **decimal fraction**: 40% is `0.40`. All energy values use `BigDecimal` with 2 decimals (kWh).
Inputs with more decimals than allowed are rejected instead of being silently rounded.

## 4. Business rules

1. **Allocation cannot exceed generation.** A household's daily share is `generation x ratio`, so the
   shares of all households add up to `generation x (sum of ratios)`. That never exceeds the day's generation
   exactly when the ratios sum to at most `1.0`. This is checked **before saving** whenever a household is created or its ratio changes.
   Nothing is adjusted silently. Error: *"Total allocated solar units cannot exceed total generated units. ..."*
   Generation logging re-checks the same rule for its date (`... for 2026-09-28.`) as a safety net.
2. **Generation share** = `dailyGeneration x allocationRatio` (rounded HALF_UP to 2 decimals).
3. **Export** = `max(generationShare - consumption, 0)`. Never negative. Calculated on the server.
   A `unitsExported` property sent by a client is ignored.
4. **Monthly summary** per household (see section 5).

Further consistency rules (so stored numbers can never disagree):

* One generation record per installation/date and one consumption record per household/date (duplicates are rejected; use `PUT`).
* Consumption needs generation for that date to exist first (the export cannot be calculated otherwise).
* Updating a generation record recalculates the exports already stored for that date. Its date cannot be changed (delete and re-create).
* A generation record cannot be deleted while consumption exists for that date.
* A household's ratio is fixed: once it has consumption records the ratio cannot change (name/owner can). Ratio changes before that are re-checked against the 100% limit.
* Deleting a household deletes its consumption; deleting an installation deletes everything under it.
* Dates cannot be in the future.
* **Concurrency:** every write takes a pessimistic lock on the installation row (`SELECT ... FOR UPDATE`) inside a transaction, so parallel requests cannot combine into an invalid allocation.

## 5. How the numbers are calculated

```
generationShare = dailyGeneration x allocationRatio
exportedUnits   = max(generationShare - consumption, 0)

Monthly, per household:
totalGenerationShare = sum over the month's generation days of (generation x ratio)
totalConsumption     = sum of the month's recorded consumption
totalExported        = sum of the month's daily exports
netUsage             = totalConsumption - totalGenerationShare
```

`netUsage < 0` means the household's allocated solar exceeded what it consumed (net surplus); `> 0` means it used more than its share.
`totalExported` is reported separately. Note that the share counts **every day with recorded generation**, even if the household logged no consumption that day.
The allocation ratios are treated as fixed for an installation, so the current ratio is applied to the whole month.

Worked example (Green Valley, generation 1000 kWh):

| Household | Ratio | Share | Consumed | Exported |
|---|---|---|---|---|
| House A | 0.40 | 400.00 | 350 | 50.00 |
| House B | 0.30 | 300.00 | 200 | 100.00 |
| House C | 0.20 | 200.00 | 150 | 50.00 |
| House D | 0.10 | 100.00 | 50 | 50.00 |

## 6. Run it

Prerequisites: JDK 17+, Maven 3.9+, Docker (for PostgreSQL).

```bash
docker compose up -d          # PostgreSQL on localhost:5432 (db/user/password: solarshare)
mvn spring-boot:run           # API on http://localhost:8080
```

Build and test:

```bash
mvn clean package             # compiles, runs all tests, builds target/solarshare-1.0.0.jar
mvn test                      # tests only (they use in-memory H2, no PostgreSQL needed)
java -jar target/solarshare-1.0.0.jar
```

Database configuration (environment variables, defaults match `docker-compose.yml`):

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/solarshare` |
| `DB_USERNAME` | `solarshare` |
| `DB_PASSWORD` | `solarshare` |

Example: `DB_URL=jdbc:postgresql://db.example.com:5432/solar DB_USERNAME=app DB_PASSWORD=secret mvn spring-boot:run`

The docker-compose credentials are for local development only. Use real secrets via environment variables elsewhere.

## 7. Swagger and Postman

* Swagger UI: <http://localhost:8080/swagger-ui.html>
* OpenAPI JSON: <http://localhost:8080/v3/api-docs>
* Postman: import `postman/SolarShare-API.postman_collection.json` and run folders 1 to 6 in order.
  IDs are stored in collection variables automatically and dates are set to "yesterday". Folder 5 shows the business-rule failures.

## 8. API endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/installations` | Create installation |
| GET | `/api/installations` | List installations |
| GET | `/api/installations/{id}` | Get installation |
| PUT | `/api/installations/{id}` | Update installation |
| DELETE | `/api/installations/{id}` | Delete installation (cascade) |
| POST | `/api/installations/{installationId}/generation` | Log daily generation |
| GET | `/api/installations/{installationId}/generation` | List generation logs |
| GET | `/api/installations/{installationId}/generation/{date}` | Generation for a date (`yyyy-MM-dd`) |
| PUT | `/api/generation/{id}` | Update generation |
| DELETE | `/api/generation/{id}` | Delete generation |
| POST | `/api/installations/{installationId}/households` | Create household |
| GET | `/api/installations/{installationId}/households` | List households |
| GET | `/api/households/{id}` | Get household |
| PUT | `/api/households/{id}` | Update household |
| DELETE | `/api/households/{id}` | Delete household |
| POST | `/api/households/{householdId}/consumption` | Record consumption |
| GET | `/api/households/{householdId}/consumption` | Consumption history |
| GET | `/api/households/{householdId}/consumption/{date}` | Consumption for a date |
| PUT | `/api/consumption/{id}` | Update consumption |
| DELETE | `/api/consumption/{id}` | Delete consumption |
| GET | `/api/households/{householdId}/summary/monthly?year=2026&month=9` | Household monthly summary |
| GET | `/api/installations/{installationId}/summary/monthly?year=2026&month=9` | All households' monthly summaries |

Validation: names not blank; `totalCapacity` > 0; `unitsGenerated` > 0; `unitsConsumed` >= 0; `allocationRatio` in (0, 1] with at most 4 decimals;
dates in `yyyy-MM-dd`, not in the future; path IDs positive; `year` 2000-2100; `month` 1-12.

## 9. Example requests and responses

```bash
curl -X POST localhost:8080/api/installations -H 'Content-Type: application/json' \
  -d '{"name":"Green Valley Housing Community","location":"Chennai","totalCapacity":250.00}'

curl -X POST localhost:8080/api/installations/1/households -H 'Content-Type: application/json' \
  -d '{"householdName":"House A","ownerName":"John","allocationRatio":0.40}'
# -> {"id":1,"installationId":1,"householdName":"House A","ownerName":"John","allocationRatio":0.40,"createdAt":"..."}

curl -X POST localhost:8080/api/installations/1/generation -H 'Content-Type: application/json' \
  -d '{"generationDate":"2026-09-27","unitsGenerated":1000.00}'
# -> {"id":1,"installationId":1,"generationDate":"2026-09-27","unitsGenerated":1000.00,"createdAt":"..."}

curl -X POST localhost:8080/api/households/1/consumption -H 'Content-Type: application/json' \
  -d '{"consumptionDate":"2026-09-27","unitsConsumed":350.00}'
# -> {"id":1,"householdId":1,"consumptionDate":"2026-09-27","unitsConsumed":350.00,"unitsExported":50.00,"createdAt":"..."}

curl "localhost:8080/api/households/1/summary/monthly?year=2026&month=9"
# -> {"householdId":1,"householdName":"House A","year":2026,"month":9,
#     "totalGenerationShare":400.00,"totalConsumption":350.00,"totalExported":50.00,"netUsage":-50.00}
```

(The example assumes House A is the only household with generation logged for that month, as above.)

### Example business-rule failure

With ratios already at 0.40 + 0.30 + 0.20 + 0.10 = 1.00, adding another household:

```bash
curl -X POST localhost:8080/api/installations/1/households -H 'Content-Type: application/json' \
  -d '{"householdName":"House E","ownerName":"Eve","allocationRatio":0.05}'
```
```json
{
  "timestamp": "2026-09-28T12:00:00",
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Total allocated solar units cannot exceed total generated units. Allocation ratios of installation 1 would add up to 1.0500, but the maximum is 1.0000 (100%).",
  "path": "/api/installations/1/households"
}
```

Other errors: `404 Not Found` (unknown id), `400 Validation Failed` (with a `fieldErrors` map), `400 Invalid Parameter`, `400 Malformed Request`, `409 Data Integrity Violation`. Stack traces are never returned.

## 10. Tests

`mvn test` runs:

* `EnergyCalculatorTest`: share, export (never negative), net usage, the Green Valley sample numbers
* Service tests (Mockito): installation/household creation, allocation over 100% rejected, duplicate generation, consumption and export, recalculation on update, monthly summary
* Controller tests (MockMvc): validation errors, invalid ratios, 404s, invalid dates/parameters, structured error body
* `SolarShareIntegrationTest`: the full Green Valley flow against H2 through the real controllers, services and JPA mappings

## 11. Project structure

```
.
├── pom.xml
├── docker-compose.yml
├── README.md
├── postman/SolarShare-API.postman_collection.json
└── src
    ├── main
    │   ├── java/com/solarshare
    │   │   ├── SolarShareApplication.java
    │   │   ├── config/OpenApiConfig.java
    │   │   ├── controller/   Installation, GenerationLog, Household, ConsumptionLog, UsageSummary
    │   │   ├── service/      the same five + EnergyCalculator
    │   │   ├── repository/   Installation, GenerationLog, Household, ConsumptionLog
    │   │   ├── entity/       Installation, GenerationLog, Household, ConsumptionLog
    │   │   ├── dto/          requests, responses, UsageSummaryResponse, ErrorResponse
    │   │   └── exception/    ResourceNotFoundException, BusinessRuleException, GlobalExceptionHandler
    │   └── resources/application.properties
    └── test
        ├── java/com/solarshare/{service,controller,integration}
        └── resources/application-test.properties
```
