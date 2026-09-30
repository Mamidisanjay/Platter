# Platter

Platter is a food-ordering platform with a preserved Vite frontend and a Spring Boot backend.

## Current stack

- Frontend: Vite, TypeScript, Axios
- Backend: Java 21, Spring Boot 3.5, Maven
- Persistence: PostgreSQL with Flyway migrations
- Cache and locks: Redis
- Search: Elasticsearch
- Auth: Spring Security, JWT, BCrypt, refresh tokens
- Payments: Stripe PaymentIntents and signed webhooks
- Realtime: WebSocket/STOMP
- Testing: JUnit 5, Mockito, Spring Boot Test dependencies

## Implemented backend areas

Restaurant and menu APIs, cached restaurant/menu reads, JWT authentication, persistent carts, transactional order creation, inventory reservation with Redis locks, Stripe payment records and webhooks, order/payment idempotency, promotions with scheduled expiry, Elasticsearch restaurant search, Bucket4j plus Redis rate limiting, audit logs, and customer favorites.

PostgreSQL remains the source of truth. Elasticsearch synchronization is best effort after committed PostgreSQL writes and has a scheduled reconciliation pass.

## Local development

The frontend can be run with:

```powershell
npm install
npm run dev
```

The backend can be built and tested with:

```powershell
cd backend
mvn clean test
```

The backend targets Java 21. Copy [backend/.env.example](backend/.env.example) to a local environment file and provide service credentials when using PostgreSQL, Redis, Elasticsearch, Stripe, or Google Maps integrations.

## API documentation

When the backend is running, Swagger UI is available at `/swagger-ui.html` and Actuator health is available at `/actuator/health`.

## Docker

A Docker Compose configuration is prepared for PostgreSQL, Redis, Elasticsearch, and the backend. Docker Desktop is required before `docker compose up --build` or container smoke tests can be run. Docker has not been claimed as tested in this environment.

## Security notes

Secrets are environment-driven and `.env` files are ignored. Do not commit real JWT secrets, database passwords, Stripe keys, Google Maps keys, or Elasticsearch credentials.
