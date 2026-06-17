# Student Management System (SMS)

A Spring Boot microservices-based Student Management System with Eureka service discovery, Spring Cloud Gateway + JWT security, Feign inter-service communication, Swagger API docs, and Docker containerization (GCP-ready).

## Architecture

```
Client → API Gateway (8080, JWT) → Eureka (8761)
                                      ├── auth-service (8081) → auth_db
                                      ├── admin-service (8082) → admin_db
                                      ├── student-service (8083) → student_db
                                      ├── teacher-service (8084) → teacher_db
                                      ├── course-service (8085) → course_db
                                      └── notification-service (8086) → notification_db
```

## Services

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| eureka-server | 8761 | — | Service discovery |
| api-gateway | 8080 | — | Routing + JWT validation |
| auth-service | 8081 | auth_db | Login, registration codes, JWT issuance |
| admin-service | 8082 | admin_db | User/course/notification management, email |
| student-service | 8083 | student_db | Student profiles + dashboard |
| teacher-service | 8084 | teacher_db | Teacher profiles + dashboard |
| course-service | 8085 | course_db | Course CRUD |
| notification-service | 8086 | notification_db | Notification CRUD |

## Registration Flow

1. **Admin** creates a student/teacher/admin via Admin API.
2. Admin service creates the profile in the target service and requests a **6–7 digit code** (valid 7 days) from Auth service.
3. An **email** is sent with a registration link and code (MailHog in local Docker).
4. User opens the link, **validates the code**, then **sets a password**.
5. Auth service activates the profile and returns a JWT for immediate login.

## Quick Start (Docker)

> **Detailed local setup guide:** see [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md) for step-by-step instructions (Docker, IntelliJ, troubleshooting).  
> **Production deployment:** see [docs/PRODUCTION_DEPLOYMENT.md](docs/PRODUCTION_DEPLOYMENT.md) for infrastructure, security, monitoring, scaling, and go-live checklist.  
> **GCP deployment:** see [docs/GCP_DEPLOYMENT.md](docs/GCP_DEPLOYMENT.md) for GKE, Cloud SQL, Secret Manager, Cloud Build, and IAM setup.  
> **Architecture diagrams:** see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for Mermaid diagrams (render at [mermaid.live](https://mermaid.live)).  
> **Go live on GCP (M.Tech demo):** see [docs/MTECH_GCP_VM_DEPLOYMENT.md](docs/MTECH_GCP_VM_DEPLOYMENT.md) — VM + Docker Compose, no domain required.

```bash
docker compose up --build
```

Wait for all services to register in Eureka: http://localhost:8761

### Bootstrap Admin

On first startup, admin-service seeds `admin@sms.local`. Check admin-service logs for the registration link and code, or open MailHog: http://localhost:8025

Complete registration:

```bash
# 1. Validate code
curl -X POST http://localhost:8080/api/auth/register/validate-code \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sms.local","code":"YOUR_CODE"}'

# 2. Set password
curl -X POST http://localhost:8080/api/auth/register/set-password \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sms.local","code":"YOUR_CODE","password":"Admin@12345"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sms.local","password":"Admin@12345"}'
```

Use the returned JWT as `Authorization: Bearer <token>` for protected endpoints.

## Quick Start (IntelliJ + Maven)

> **Full IntelliJ walkthrough:** see [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md#option-b--run-in-intellij-with-docker-for-db--email).

**Prerequisites:** JDK 17, Maven 3.9+, PostgreSQL 16

1. Create databases (or run `docker/init-databases.sql`).
2. Start PostgreSQL and optionally MailHog on port 1025.
3. Import the root `pom.xml` as a Maven project in IntelliJ.
4. Run services in order:
   - `EurekaServerApplication`
   - `AuthServiceApplication`, `CourseServiceApplication`, `NotificationServiceApplication`
   - `StudentServiceApplication`, `TeacherServiceApplication`
   - `AdminServiceApplication`
   - `ApiGatewayApplication`

Build all modules:

```bash
mvn clean package -DskipTests
```

## Key API Endpoints (via Gateway)

### Auth (public)
- `POST /api/auth/login`
- `POST /api/auth/register/validate-code`
- `POST /api/auth/register/set-password`

### Admin (JWT required, ADMIN role expected)
- `GET /api/admin/dashboard`
- `POST /api/admin/students` — create student + send registration email
- `POST /api/admin/teachers`
- `POST /api/admin/admins`
- CRUD for `/api/admin/courses`, `/api/admin/notifications`

### Student (JWT required)
- `GET /api/students/dashboard` — courses, progress, latest notification
- `GET /api/students/me`

### Teacher (JWT required)
- `GET /api/teachers/dashboard` — courses, notifications, students
- `GET /api/teachers/me`

## Swagger UI

Each service exposes Swagger at `/swagger-ui.html`:

- Auth: http://localhost:8081/swagger-ui.html
- Admin: http://localhost:8082/swagger-ui.html
- Student: http://localhost:8083/swagger-ui.html
- Teacher: http://localhost:8084/swagger-ui.html
- Course: http://localhost:8085/swagger-ui.html
- Notification: http://localhost:8086/swagger-ui.html

## Example: Admin Creates a Student

```bash
curl -X POST http://localhost:8080/api/admin/students \
  -H "Authorization: Bearer <ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@student.local",
    "phone": "9876543210",
    "address": "123 Campus Road",
    "rollNumber": "STU001",
    "className": "CS-3A",
    "department": "Computer Science",
    "subjects": ["Data Structures", "Algorithms"]
  }'
```

## GCP Deployment Notes

- Each service is containerized and stateless; deploy to GKE or Cloud Run.
- Use Cloud SQL (PostgreSQL) with one database per service.
- Configure secrets via Secret Manager (`JWT_SECRET`, DB credentials).
- Use Cloud Pub/Sub or SendGrid for production email instead of MailHog.
- Add Cloud Monitoring / Logging in a follow-up phase.

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | Shared JWT signing key | (dev default in compose) |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Database connection | localhost / service-specific |
| `EUREKA_HOST` | Eureka server hostname | localhost |
| `REGISTRATION_BASE_URL` | Frontend registration page URL | http://localhost:3000/register |
| `MAIL_HOST`, `MAIL_PORT` | SMTP server | localhost:1025 |
