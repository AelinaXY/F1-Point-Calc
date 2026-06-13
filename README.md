# F1 Point Calc

F1 Point Calc is a Spring Boot + Spark application that predicts F1 Fantasy points from historical race/session data and
uses those predictions to suggest better fantasy teams.
Use the request’s `isSprint` context to evaluate the correct weekend mode before lock-in.
The primary user flow is **team optimisation** via `/api/job/optimalTeam`.

## Weekend Structure

Over the course of a Formula One meeting or weekend, several sessions happen.

- Free Practise 1: A 60 minute session with no points.
- Free Practise 2: A 60 minute session with no points. This session only occurs on non sprint weekends.
- Free Practise 3: A 60 minute session with no points. This session only occurs on non sprint weekends.
- Qualifying: A 60 minute session wherein a driver qualifies for the race. Their finishing order here determines the
  starting position of the race. This session is broken into Q1, Q2, Q3, where Q1 is the first 18 minutes of the
  session, Q2 is the next 15 minutes of the session, and Q3 is the final 12 minutes of the session. There is a short
  break between each. At the end of Q1 and Q2, the bottom (number of drivers - 10)/2 are eliminated.
- Race: A 120 minute session wherein the drivers race to the finish. At the end of the session, the drivers get world
  championship points based on their finishing position in the race. Points are only scored by the top ten drivers.
- Sprint Qualifying: A 60 minute session wherin a driver qualifies for the sprint race. The format is the same as the
  qualifying session except that they must use the same tire for each part of the session: a hard tire in Q1, a medium
  tire in Q2, and a soft tire in Q3. This session only occurs on sprint weekends.
- Sprint Race: A 60 minute session wherein the drivers race to the finish. At the end of the session, the drivers get
  world championship points based on their finishing position in the race. Points are only scored by the top eight
  drivers. The points scored as less than that of the race. This session only occurs on sprint weekends.

Weekend structure by whether the race is a sprint race. It follows the below table.

| Day and time of day | Non-Sprint      | Sprint            |
|---------------------|-----------------|-------------------|
| Friday Morning      | Free Practise 1 | Free Practise 1   |
| Friday Afternoon    | Free Practise 2 | Sprint Qualifying |
| Saturday Morning    | Free Practise 3 | Sprint Race       |
| Saturday Afternoon  | Qualifying      | Qualifying        |
| Sunday Afternoon    | Race            | Race              |

You are unable to change your fantasy team after Qualifying has started on non-sprint weekends, or after the Sprint Race
has started on sprint weekends.

## Fantasy Points

Fantasy points are calculated for drivers as follows:

- 1 point for every world championship point scored.
- 1 point for every on track overtake.
- Points according to the driver qualifying table below.
- 10 points for being Driver of the Day, a fan awarded vote.
- 10 points for having the fastest lap in the race.
- 5 points for having the fastest lap in the sprint session.
- 1 point for every position higher than their starting position that a driver finished.
- -1 point for every position lower than their starting position that a driver finished (to a maximum of -10).
- -20 points if a driver is not classified in the finishing order, or is disqualified from the race.
- -10 points if a driver is not classified in the finishing order, or is disqualified from the sprint race.
- -5 points if a driver is not classified in the finishing order, or is disqualified from the qualifying.

Fantasy points are calculated for a team as follows. For all of these metrics they apply to both drivers:

- 1 point for every world championship point scored.
- 1 point for every on track overtake.
- 1 point for every position higher than their starting position that a driver finished.
- -1 point for every position lower than their starting position that a driver finished (to a maximum of -10).
- -20 points if a driver is not classified in the finishing order, or is disqualified from the race.
- -10 points if a driver is not classified in the finishing order, or is disqualified from the sprint race.
- -5 points if a driver is not classified in the finishing order, or is disqualified from the qualifying.
- 10 points if either driver has the fastest lap in the race.
- 5 points if the team has fastest pit stop of the race.
- Points according to the pitstop table below.
- Points according to the team qualifying table below.
- Points according to the driver qualifying table below.

### Team Qualifying Table

| Condition                 | Points |
|---------------------------|--------|
| Neither driver reaches Q2 | -1     |
| One driver reaches Q2     | 1      |
| Both drivers reach Q2     | 3      |
| One driver reaches Q3     | 5      |
| Both drivers reach Q3     | 10     |

### Driver Qualifying Table

| Position    | Points |
|-------------|--------|
| 1st         | 10     |
| 2nd         | 9      |
| 3rd         | 8      |
| 4th         | 7      |
| 5th         | 6      |
| 6th         | 5      |
| 7th         | 4      |
| 8th         | 3      |
| 9th         | 2      |
| 10th        | 1      |
| 11th - 20th | 0      |

### Sprint World Championship Points Table

| Position   | Points |
|------------|--------|
| 1st        | 8      |
| 2nd        | 7      |
| 3rd        | 6      |
| 4th        | 5      |
| 5th        | 4      |
| 6th        | 3      |
| 7th        | 2      |
| 8th        | 1      |
| 9th - 20th | 0      |

### Race World Championship Points Table

| Position    | Points |
|-------------|--------|
| 1st         | 25     |
| 2nd         | 18     |
| 3rd         | 15     |
| 4th         | 12     |
| 5th         | 10     |
| 6th         | 8      |
| 7th         | 6      |
| 8th         | 4      |
| 9th         | 2      |
| 10th        | 1      |
| 11th - 20th | 0      |

### Pitstop Table

This is based on the teams fastest pit stop.

| Time       | Points   |
|------------|----------|
| Over 3s    | 0        |
| 2.5-2.99s  | 2        |
| 2.2-2.49s  | 5        |
| 2.0-2.19s  | 10       |
| Under 2.0s | 20       |
| Under 1.8s | Bonus 15 |

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
    - OpenF1 bearer token: set `openf1.bearer-token` (used as `Authorization: Bearer <token>` on OpenF1 requests)

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
