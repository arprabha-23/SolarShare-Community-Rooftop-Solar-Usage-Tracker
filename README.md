# SolarShare – Community Rooftop Solar Usage Tracker

A full-stack college project for tracking shared rooftop solar generation, household allocations, consumption, exports and monthly community performance.

## Stack
- Java 17
- Spring Boot 3.3.x
- Spring Web + REST
- Spring Data JPA / Hibernate
- MySQL
- Maven
- HTML + CSS + Vanilla JavaScript
- Chart.js via CDN
- Swagger/OpenAPI
- Postman

## Architecture
`Browser → fetch() → Spring REST Controller → Service (business logic) → JPA Repository → MySQL`

Important energy calculations happen in `SolarShareService`, not only in JavaScript.

For a 1000 kWh monthly generation and a 40% household allocation:
`Allocated = 1000 × 0.40 = 400 kWh`.
If consumption is 350 kWh, export is `max(400 - 350, 0) = 50 kWh`; if consumption exceeds allocation, the service reports additional energy needed.

## Database setup
1. Install MySQL 8.x and start the server.
2. Create the database:
   `CREATE DATABASE solarshare;`
3. Open `src/main/resources/application.properties`.
4. Replace `YOUR_MYSQL_PASSWORD` with your MySQL root password.
5. Hibernate uses `ddl-auto=update` and creates/updates tables automatically.
6. Optional sample records are in `database/solarshare_sample_data.sql`.

## Run
Requirements: JDK 17+, Maven 3.9+, MySQL.

```bash
mvn clean
mvn spring-boot:run
```

Or build a jar:
```bash
mvn clean package
java -jar target/solarshare-1.0.0.jar
```

No Node.js or frontend build step is required.

## URLs
- Dashboard: http://localhost:8080/
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## REST API
### Installations
- POST `/api/installations`
- GET `/api/installations`
- GET `/api/installations/{id}`
- PUT `/api/installations/{id}`
- DELETE `/api/installations/{id}`
- GET `/api/installations/{id}/dashboard`
- GET `/api/installations/{id}/summary/monthly`

### Households
- POST `/api/installations/{installationId}/households`
- GET `/api/installations/{installationId}/households`
- GET `/api/households/{id}`
- PUT `/api/households/{id}`
- DELETE `/api/households/{id}`
- GET `/api/households/{id}/summary/monthly`

### Energy records
- POST `/api/installations/{installationId}/generation`
- GET `/api/installations/{installationId}/generation`
- POST `/api/households/{householdId}/consumption`
- GET `/api/households/{householdId}/consumption`

## Example JSON
Installation:
```json
{"name":"SolarShare Community","location":"Coimbatore, Tamil Nadu","capacityKw":250}
```
Household:
```json
{"name":"House A","ownerName":"Arun","allocationRatio":0.40}
```
Generation/consumption:
```json
{"date":"2026-09-28","units":1000}
```

## Postman
Import `postman/SolarShare.postman_collection.json`. Set `installationId` and `householdId` variables after creating records.

## Frontend
The UI is served directly by Spring Boot from `src/main/resources/static`. It uses reusable `apiGet`-style behavior through the `api()` helper and browser `fetch()` calls. There is no React, Angular, Vue, Node.js or TypeScript.

## Project structure
```text
SolarShare-Community-Rooftop-Solar-Usage-Tracker/
├── pom.xml
├── README.md
├── database/solarshare_sample_data.sql
├── postman/SolarShare.postman_collection.json
└── src/main/
    ├── java/com/solarshare/
    │   ├── config/
    │   ├── controller/
    │   ├── dto/
    │   ├── entity/
    │   ├── exception/
    │   ├── repository/
    │   ├── service/
    │   └── SolarShareApplication.java
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html
            ├── style.css
            └── script.js
```

## Troubleshooting
- `Communications link failure`: make sure MySQL is running and the port is 3306.
- `Access denied`: check the username/password in `application.properties`.
- `Table doesn't exist`: restart once with `spring.jpa.hibernate.ddl-auto=update`.
- Blank dashboard with no installations: create one from **Installations**.
- Swagger unavailable: confirm the application started without Maven dependency errors.

## Notes
This project intentionally does not include `target/`, IDE metadata, `node_modules/`, or other generated files in the distribution ZIP.
