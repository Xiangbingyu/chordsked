# AGENTS.md

## Project Structure

```
chordsked/
├── frontend/          # React + Vite + TypeScript + Ant Design 5 + Zustand
├── backend/           # Spring Boot 4.0.0 + MyBatis + MySQL/H2 + Redis
├── docker-compose.yml # Redis only (port 6380)
└── doc/             # Requirements docs
```

## Developer Commands

### Frontend
```bash
cd frontend
npm run dev              # dev mode, proxy to localhost:8080
npm run dev:test        # test mode, proxy to localhost:8081
npm run dev:prod      # production mode
npm run build          # tsc -b && vite build
npm run lint           # ESLint check
npm run preview        # preview production build
```

### Backend
```bash
cd backend
./mvnw spring-boot:run           # Run app (default profile)
./mvnw test                   # Run tests
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Key Config Files

- `frontend/vite.config.ts` - Vite proxy config (API -> backend)
- `frontend/.env.development` / `.env.test` / `.env.production`
- `backend/src/main/resources/application.yml` - Main config
- `backend/src/main/resources/env/dev|test|prerelease|online/` - Environment configs
- `backend/src/main/resources/db/mysql/schema.sql` - MySQL schema
- `backend/src/main/resources/db/h2/schema.sql` - H2 schema

## Skills (Auto-loaded)

- `.opencode/skills/enterprise-react-frontend/` - Frontend conventions
- `.opencode/skills/enterprise-java-backend/` - Backend conventions
- `.opencode/skills/karpathy-guidelines/` - Code quality guidelines

## Architecture Notes

- Backend uses `@Resource` with explicit `name` for DI (e.g., `@Resource(name = "userService")`)
- Service layer uses `@Transactional(rollbackFor = Exception.class)`
- API paths use `/api/v1/` prefix
- Frontend uses Zustand for state, Ant Design 5 for UI
- API uses `open` instead of `visible` for Modal/Drawer (Antd 5 API)
- Redis runs on port 6380 (not default 6379)

## Testing

- Backend tests in `src/test/java/` following Spring Boot test patterns
- Run single test: `./mvnw test -Dtest=ClassName`
- Frontend has no dedicated test framework configured