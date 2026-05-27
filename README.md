# F1 Point Calc

F1 Point Calc is a Spring Boot + Spark application that predicts F1 Fantasy points from historical race/session data and uses those predictions to suggest better fantasy teams.
Use the request’s `isSprint` context to evaluate the correct weekend mode before lock-in.
The primary user flow is **team optimisation** via `/api/job/optimalTeam`.

## Team lock rule

You are unable to change your team if the following conditions are met:
- Qualifying has started.
- The sprint race has started.

## Installation and requirements

### Runtime requirements

- Java 21
- Maven 3.9+
- Docker (for local Postgres)
- PostgreSQL (or `docker-compose` below)

### Local setup

1. Start database:

```bash
docker compose up -d postgres-db
```

2. Configure app/database properties (defaults already in `src/main/resources/application.properties`):
   - DB: `jdbc:postgresql://localhost:5432/f1`
   - user: `f1`
   - password: `pass`
   - OpenF1 base URL: `https://api.openf1.org/v1/`

3. Build:

```bash
./mvn clean compile
```

4. Run:

```bash
./mvn spring-boot:run
```

## Birds-eye architecture

- **API layer**: controllers under:
  - `/api/job` (optimisation + diagnostics)
  - `/api/openf1` (data ingestion)
  - `/api/regression` (feature generation + model training)
- **Service layer**: orchestration for ingestion, NSAD feature generation, and training.
- **Persistence layer**: PostgreSQL + Flyway migrations + jOOQ repositories.
- **ML layer**:
  - `ScoreCalculatorV3` for runtime prediction,
  - `RegressionService` + `EvaluationResult` for training/tuning,
  - model artifacts in `src/main/resources/regressionModel2`.

For detailed diagrams, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Features and interfaces

### 1) Primary endpoint: optimise a team

`POST /api/job/optimalTeam`

- Query params:
  - `driverList` (repeatable)
  - `teamList` (repeatable)
  - `changeLimit` (default `2`)
- JSON body (`OptimalTeamRequest`):

```json
{
  "costCap": 100.0,
  "transferLimit": 2,
  "raceName": "Singapore",
  "isSprint": false,
  "racesLeft": 6,
  "costCapMult": 1.2
}
```

Returns:
- original team,
- ranked candidate scorecards,
- differences (in/out changes, score/cost deltas).

### 2) Debug prediction endpoint

`GET /api/job/predict?raceName=Singapore&isSprint=false`

Used for diagnostics/comparison, not primary product flow.

### 3) Regression and model training

- `GET /api/regression/nsad`  
  Populates NSAD feature rows from historical datasets + OpenF1-backed metadata.

- `GET /api/regression/trainNSAD?controlOnly=false`  
  Trains model and returns hyperparameters, MSE, and feature importance.

`controlOnly` defaults to `false`.  
When `true`, training evaluates only the control/baseline hyperparameter set.

### 4) OpenF1 ingestion endpoints

- `GET /api/openf1/sessions`
- `GET /api/openf1/meetings`
- `GET /api/openf1/drivers`
- `GET /api/openf1/sessionResults`
- `GET /api/openf1/populate` (runs full ingest sequence + NSAD population)

## Development

### Build / run

```bash
mvn clean compile
mvn spring-boot:run
```

### Tests

```bash
mvn test
```

### Database schema lifecycle

Schema is managed through Flyway SQL files in:

- `src/main/resources/migration`

jOOQ code generation runs in Maven `generate-sources` phase (configured in `pom.xml`).

## Operational notes

- Spark runs locally (`local[*]`) via `SparkConfig`.
- Training overwrites and reloads model artifact at:
  - `src/main/resources/regressionModel2`
- Ingestion uses OpenF1 with HTTP rate limiting and maps upstream failures to HTTP 502.
- CSV resources under `src/main/resources` are part of feature/training data inputs.
