# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ChordSked is a music education scheduling system built with a modern full-stack architecture:

- **Frontend**: React 18 + Vite + TypeScript + Ant Design 5 + Zustand
- **Backend**: Spring Boot 4.0.0 + MyBatis + MySQL/H2 + Redis + Spring Security
- **Architecture**: Monolithic with clear layer separation (Controller → Service → DAO)

## Development Commands

### Frontend
```bash
cd frontend
npm run dev              # Development mode (proxies to localhost:8081)
npm run dev:test        # Test mode (proxies to test backend)
npm run dev:prod        # Production mode
npm run build           # Production build (tsc -b && vite build)
npm run lint            # ESLint check
npm run preview         # Preview production build
```

### Backend
```bash
cd backend
./mvnw spring-boot:run  # Run with default profile
./mvnw test            # Run tests
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Infrastructure
```bash
docker-compose up -d    # Start Redis on port 6380
```

## Architecture & Structure

### Backend Layer Pattern
```
controller/  →  service/  →  dao/  →  database
   (HTTP)       (business)   (data)
```

**Key Rules**:
- Controllers only handle HTTP concerns (routing, validation, response formatting)
- Services contain business logic and transaction boundaries
- DAOs handle persistence only (no business logic)
- Use `@Resource(name = "beanName")` for all dependency injection
- Services must use `@Transactional(rollbackFor = Exception.class)` for write operations

### Frontend Structure
- **Feature-based**: `features/admin/org/org-account/` for complete features
- **Shared components**: Reusable UI components in separate directories
- **State management**: Zustand stores in `stores/`
- **API layer**: Service modules in `services/`

### Database
- **Development**: H2 in-memory database (schema auto-loaded from `db/h2/schema.sql`)
- **Production**: MySQL (configure via environment variables)
- **ORM**: MyBatis with XML mappers in `resources/mapper/`

## Configuration

### Environment Profiles
Backend uses Spring profiles: `dev`, `test`, `prerelease`, `online`
- Config files: `resources/env/{profile}/application-{profile}.yml`
- Common config: `resources/common/application-common.yml`

### Frontend Environment
- `.env.development` - Development (API: http://localhost:8081)
- `.env.test` - Test environment
- `.env.production` - Production

### Key Configuration Details
- **Redis**: Port 6380 (not default 6379)
- **API prefix**: `/api/v1/`
- **Admin API**: `/admin/api/v1/`
- **JWT**: Used for authentication with Redis-backed sessions
- **Data scope**: Row-level security via `/*DATA_SCOPE*/` placeholder

## Important Patterns

### Backend Service Pattern
```java
@Service("serviceName")
public class ServiceImpl implements Service {
    @Resource(name = "daoName")
    private Dao dao;
    
    @Override
    @AuditLog(module = "MODULE", action = "ACTION")
    @Transactional(rollbackFor = Exception.class)
    public Result method(Request request) {
        // Business logic
    }
}
```

### Frontend API Pattern
```typescript
import http from './http'

export const apiMethod = (params: ParamsType) =>
  http.post<ResponseType>('/endpoint', params)
```

### Ant Design 5 Migration
- Use `open` instead of `visible` for Modal/Drawer
- Use `onOpenChange` instead of `onVisibleChange`

## Skills & Conventions

The project includes auto-loaded skills that enforce enterprise standards:
- **`.opencode/skills/enterprise-java-backend/`** - Backend patterns (Controller/Service/DAO, validation, security)
- **`.opencode/skills/enterprise-react-frontend/`** - Frontend patterns (React 18, Antd 5, Zustand)
- **`.opencode/skills/karpathy-guidelines/`** - Code quality guidelines

These skills are automatically activated when working on relevant code types.

## Security & Authentication

- **JWT-based authentication** with Redis session management
- **Role-based access control** via `@PreAuthorize` annotations
- **Protected roles**: SYSTEM_ADMIN cannot be modified/deleted
- **Password security**: BCrypt encryption, mandatory change on first login
- **Audit logging**: All critical operations logged via `@AuditLog` annotation

## Testing

- **Backend**: JUnit 5 tests in `src/test/java/`
- **Test execution**: `./mvnw test` or `./mvnw test -Dtest=ClassName`
- **Frontend**: No dedicated test framework currently configured

## Common Tasks

### Adding a New API Endpoint
1. Create DTO/VO classes in `model/dto/` and `model/vo/`
2. Create/update entity in `model/entity/`
3. Add/update MyBatis mapper in `dao/` and `resources/mapper/`
4. Implement service interface and implementation in `service/`
5. Add controller method with Swagger annotations
6. Add audit log annotation if applicable

### Adding a New Frontend Page
1. Create feature directory under `features/`
2. Add page component and sub-components
3. Create API service methods
4. Add route in `routes/AppRouter.tsx`
5. Update navigation config if needed

### Database Schema Changes
1. Update `resources/db/h2/schema.sql` (and MySQL equivalent)
2. Update corresponding entity classes
3. Update MyBatis mappers
4. Test with both H2 and MySQL if applicable

## Error Handling

- **Backend**: Global exception handlers in `exception/` package
- **Frontend**: HTTP interceptor in `services/http.ts` redirects to login on 401
- **Business errors**: Use `BusinessException` with error codes

## Logging

- **Backend**: SLF4J with structured logging (includes traceId/requestId)
- **Pattern**: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [traceId=%X{traceId} requestId=%X{requestId}] - %msg%n`
- **Never log**: Passwords, tokens, or other sensitive data
