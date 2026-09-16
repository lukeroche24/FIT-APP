# FIT

Strength training tracker: exercises, workout templates, session logs, weekly plans, and friend activity. Prescriptions use double progression (add a rep, then add load), with a hold after time off and a deload after a longer gap. Rep-range changes convert through an estimated 1RM.

University of Glasgow MSc IT project. Register an account to use the app. There is no seeded demo user.

## Features

- Email/password accounts and JWT sessions
- Exercise library (barbell, dumbbell, machine, bodyweight, unilateral logging)
- Workout templates with planned sets, then in-progress and completed logs
- Next-set suggestions from the last session
- Weekly plans with a calendar of scheduled days
- Friends, friend profiles, and a feed of completed sessions
- Estimated and tested 1RM lookup

## Stack

| | |
| --- | --- |
| API | Spring Boot 4, Java 21, PostgreSQL, JWT |
| Web | React 19, TypeScript, Vite, Bootstrap 5 |
| Tests | JUnit (Maven), Vitest |

The Java package is `com.lukeroche.fit`.

## Prerequisites

- Java 21
- Node.js 20 or later
- PostgreSQL, with a database named `test` (or change the URL in config)

Default local database settings in `backend/src/main/resources/application.properties`:

```
jdbc:postgresql://localhost:5432/test
username: postgres
password: password
```

Tables are created and updated with Hibernate (`ddl-auto=update`).

## Run locally

API (default [http://localhost:8080](http://localhost:8080)):

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

macOS / Linux: `./mvnw spring-boot:run`.

Web app (Vite, [http://localhost:5173](http://localhost:5173)):

```powershell
cd frontend
npm install
npm run dev
```

Copy `frontend/.env.example` to `frontend/.env` if the API is not on `http://localhost:8080`. The only variable is `VITE_API_URL`.

## Configuration

| Setting | Where | Notes |
| --- | --- | --- |
| `jwt.secret` | `application.properties` | Must be at least 32 bytes. Change this before deploying. |
| `jwt.expiry-ms` | `application.properties` | Access token lifetime (default 24 hours). |
| Datasource | `application.properties` | Local Postgres. |
| `fit.progression.*` | `application.properties` | Deload factor, hold after 14 days, deload after 28 days. |
| `fit.strength.estimated-one-rm-days` | `application.properties` | Window for estimated 1RM. |
| CORS | `SecurityConfig` | Allows `http://localhost:5173` and the Render frontend origin. |

## Tests

```powershell
cd backend
.\mvnw.cmd test
```

```powershell
cd frontend
npm test
```

Backend tests use an in-memory H2 database. They do not need Postgres.

## Deploy

The API is a Docker image. `backend/Dockerfile` builds the Spring Boot jar. On Render, set the Docker context to `backend`.

Point the frontend `VITE_API_URL` at the deployed API and rebuild. Keep `jwt.secret` out of git for a real environment.

## Layout

```
backend/     Spring Boot API
frontend/    Vite React app
```

## AI usage

I used Cursor as a guide while writing code (design, naming, and whether an approach was sound). This was my first time using TypeScript and React. I wrote the frontend myself as best I could, then used Cursor to help check it was correct and to help with tidying it up.

I wrote the source myself except where a file marks a section as `[AI-GENERATED]`.

I decided which behaviours to test, then used Cursor to write the test files. I reviewed them and they pass. That is declared in each test file header.

Comments were drafted by me and then touched up with AI. That is declared in each affected file header.
