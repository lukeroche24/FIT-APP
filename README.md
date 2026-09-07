# FIT

Workout tracking app for the University of Glasgow MSc IT project.

- **`backend/`** — Spring Boot 21 API (PostgreSQL, JWT)
- **`frontend/`** — React + TypeScript (Vite)

The Java package stays `com.lukeroche.fit`. If you deploy on Render, set the Docker context to `backend`.

## Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Uses `backend/src/main/resources/application.properties` (local Postgres). Tests:

```powershell
.\mvnw.cmd test
```

## Frontend

```powershell
cd frontend
npm install
npm run dev
```

Copy `frontend/.env.example` to `.env` if you need a non-default API URL (`VITE_API_URL`). Tests:

```powershell
npm test
```
