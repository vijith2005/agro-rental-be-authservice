# UserAuthService

Spring Boot auth and user service for AgroConnect.

## Stack
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA (MySQL)
- Eureka Client + OpenFeign

## Run
1. Ensure MySQL is running and database `spring_security_db` exists.
2. Ensure Eureka server is running at `http://localhost:8761`.
3. Configure credentials in `src/main/resources/application.properties`.
4. Run:

```bash
mvn spring-boot:run
```

## Base URLs
- Service: `http://localhost:8081`
- Auth base: `http://localhost:8081/api/v1/auth`
- Users base: `http://localhost:8081/api/v1/users`

## Seeded Admin
- Email: `admin@agroconnect.com`
- Password: `Admin@123`

## Main APIs
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`
- `PUT /api/v1/users/me`
- `GET /api/v1/users/{id}`
- `GET /api/v1/users` (ADMIN)
- `PATCH /api/v1/users/{id}/status` (ADMIN)

