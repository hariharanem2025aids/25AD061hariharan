# CarbonTrack — Household Carbon Footprint Calculator with Challenges

Spring Boot (Maven) backend with a plain HTML/CSS/JS frontend. Households log daily
activities (commute, electricity, waste), the service computes an estimated monthly
carbon footprint from a fixed emission-factor table, and households compete in
community reduction challenges and a monthly leaderboard.

## Project structure

| Package      | Classes |
|--------------|---------|
| Controller   | HouseholdController, ActivityLogController, FootprintController, ChallengeController, ParticipationController, LeaderboardController |
| Services     | HouseholdServices, ActivityLogServices, FootprintServices, ChallengeServices, ParticipationServices, LeaderboardServices |
| Respository  | HouseholdRepository, ActivityLogRepository, ChallengeRepository, ParticipationRepository |
| Models       | Household, ActivityLog, Challenge, Participation, ActivityType, ActivityCategory, HouseholdStatus, ChallengeStatus, ParticipationStatus |
| Dto          | ActivityLogRequest, ParticipationRequest, MonthlyFootprint, MonthlyTrend, LeaderboardEntry, LeaderboardResponse |
| Exception    | ResourceNotFoundException, DuplicateResourceException, BadRequestException, ErrorResponse, GlobalExceptionHandler |
| Config       | CorsConfig |
| Data         | SampleDataLoader |

Frontend files: `src/main/resources/static/` (`index.html`, `styles.css`, `app.js`), served at `http://localhost:8081/`.

## Entity relationships

- **Household** 1 — * **ActivityLog** (each log belongs to one household)
- **Household** 1 — * **Participation**
- **Challenge** 1 — * **Participation**
- **Participation** joins one Household to one Challenge (unique per pair) and stores the baseline, current footprint, reduction % and status.

## Emission-factor table (fixed)

Defined once in `Models/ActivityType.java` and exposed at `GET /api/footprint/factors`.
Emission (kg CO2e) = quantity × factor. The factor used is stored on every log.

| Activity type       | Unit | kg CO2e per unit | Max per single entry |
|---------------------|------|------------------|----------------------|
| COMMUTE_CAR         | km   | 0.192            | 500                  |
| COMMUTE_MOTORCYCLE  | km   | 0.103            | 500                  |
| COMMUTE_BUS         | km   | 0.089            | 1000                 |
| COMMUTE_TRAIN       | km   | 0.041            | 1000                 |
| COMMUTE_WALK_CYCLE  | km   | 0.000            | 100                  |
| ELECTRICITY         | kWh  | 0.820            | 500                  |
| WASTE               | kg   | 0.500            | 200                  |

## Monthly footprint estimate

- Complete month: the sum of the emissions logged in that month.
- Month in progress: the logged total is projected to the full month
  (`logged total ÷ days elapsed × days in month`) so it can be compared fairly with a full previous month.

## Business rules (enforced in the service layer)

Activity logs
- Only the fixed emission-factor table is used; emission is computed by the server, never accepted from the client.
- Quantity must be greater than zero and within the per-entry maximum for that activity type.
- Log date cannot be in the future or more than 12 months in the past.
- Inactive households cannot log activities.

Challenges
- Target reduction must be between 1 and 100 percent; end date must be after start date.
- A new challenge cannot end in the past; titles are unique.
- Target and dates cannot be edited, and the challenge cannot be deleted, once households have joined.
- Status is computed from dates: UPCOMING, ACTIVE, COMPLETED.

Participation
- A household can join a challenge only once, and only if the challenge has not ended and the household is active.
- The household must have logged activity in the baseline month (the month before the challenge starts).
- Progress = % reduction of the current month's estimate versus the baseline; status becomes ACHIEVED when it reaches the target, FAILED if the challenge ends without reaching it.
- Every status change is written to the console as a `[NOTIFY]` log line.

Leaderboard
- Rank is based on % reduction versus the household's own previous month, not absolute footprint.
- Only active households with logs in both months are ranked; equal percentages share a rank.
- Defaults to the last completed month.

## REST endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST / GET / PUT / DELETE | `/api/household/create`, `/getall`, `/getbyid/{id}`, `/update`, `/delete/{id}` | Households |
| POST / GET / PUT / DELETE | `/api/activity/create`, `/getall`, `/getbyid/{id}`, `/byhousehold/{id}`, `/update`, `/delete/{id}` | Activity logs |
| GET | `/api/footprint/factors` | Emission-factor table |
| GET | `/api/footprint/household/{id}?month=yyyy-MM` | Monthly footprint with breakdown |
| GET | `/api/footprint/history/{id}?months=6` | Month-by-month trend |
| POST / GET / PUT / DELETE | `/api/challenge/create`, `/getall`, `/getbyid/{id}`, `/update`, `/delete/{id}` | Challenges |
| POST | `/api/participation/join` | Join a challenge (`householdId`, `challengeId`) |
| GET | `/api/participation/getall`, `/getbyid/{id}`, `/byhousehold/{id}`, `/bychallenge/{id}` | Progress (challenge view sorted by reduction) |
| PUT | `/api/participation/progress/{id}` | Recalculate progress |
| DELETE | `/api/participation/withdraw/{id}` | Leave a challenge |
| GET | `/api/leaderboard?month=yyyy-MM` | Monthly reduction leaderboard |

Errors return JSON: `{ "status": 400, "error": "Bad Request", "message": "...", "timestamp": "..." }`
(validation errors also include a `fields` map). Status codes used: 201, 400, 404, 409.

### Example requests

```
POST /api/activity/create
{ "householdId": 1, "logDate": "2026-09-20", "activityType": "COMMUTE_CAR", "quantity": 25 }

POST /api/challenge/create
{ "title": "Zero Waste Week", "targetReductionPercent": 12, "startDate": "2026-10-01", "endDate": "2026-10-31" }

POST /api/participation/join
{ "householdId": 1, "challengeId": 1 }
```

## Running

1. Set your MySQL URL, username and password in `src/main/resources/application.properties`.
2. `mvn spring-boot:run`
3. Open `http://localhost:8081/`.

With `app.load-sample-data=true`, the first run seeds 6 households, about 5 months of activity logs, 2 challenges and their participations.
Set it to `false` to start empty. Requires Java 21.
