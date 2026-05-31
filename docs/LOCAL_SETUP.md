# Local Setup Guide — Student Management System

This guide walks you through running the full microservices stack on your **local machine** (Windows, macOS, or Linux).

There are two recommended approaches:

| Approach | Best for | Difficulty |
|----------|----------|------------|
| [Option A — Docker Compose (full stack)](#option-a--run-everything-with-docker-compose) | Fastest setup, no local DB install | Easy |
| [Option B — IntelliJ + Maven (hybrid)](#option-b--run-in-intellij-with-docker-for-db--email) | Active development and debugging | Medium |

---

## Prerequisites

Install the following before you start:

| Tool | Version | Download |
|------|---------|----------|
| **JDK** | 17+ | [Adoptium / Oracle JDK](https://adoptium.net/) |
| **Maven** | 3.9+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| **Docker Desktop** | Latest | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| **IntelliJ IDEA** | 2023+ (Community or Ultimate) | [JetBrains](https://www.jetbrains.com/idea/download/) |

Optional but useful:

- **Postman** or **curl** — for testing APIs
- **Git** — if cloning the repository

Verify installations:

```powershell
java -version
mvn -version
docker --version
docker compose version
```

---

## Project structure (quick reference)

```
NewProject/
├── pom.xml                    ← Root Maven project (import this in IntelliJ)
├── docker-compose.yml         ← Full stack Docker setup
├── docker/init-databases.sql  ← PostgreSQL database init script
├── eureka-server/             ← Port 8761
├── api-gateway/               ← Port 8080 (main entry point)
├── auth-service/              ← Port 8081
├── admin-service/             ← Port 8082
├── student-service/           ← Port 8083
├── teacher-service/           ← Port 8084
├── course-service/            ← Port 8085
└── notification-service/      ← Port 8086
```

**Main entry point for all API calls:** `http://localhost:8080` (API Gateway)

---

## Option A — Run everything with Docker Compose

This starts PostgreSQL, MailHog, Eureka, all 6 microservices, and the API Gateway in containers.

### Step 1 — Start Docker Desktop

Make sure Docker Desktop is running. On Windows, wait until the whale icon in the system tray shows **"Docker Desktop is running"**.

### Step 2 — Open a terminal in the project root

```powershell
cd d:\NewProject
```

### Step 3 — Build and start all services

```powershell
docker compose up --build
```

First run takes **5–15 minutes** (downloads images and compiles all Maven modules).

To run in the background:

```powershell
docker compose up --build -d
```

### Step 4 — Wait for services to be healthy

Watch the logs until you see each service register with Eureka. You can also open:

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| MailHog (email inbox) | http://localhost:8025 |

In the Eureka dashboard you should see **7 registered applications**:

- `API-GATEWAY`
- `AUTH-SERVICE`
- `ADMIN-SERVICE`
- `STUDENT-SERVICE`
- `TEACHER-SERVICE`
- `COURSE-SERVICE`
- `NOTIFICATION-SERVICE`

### Step 5 — Complete bootstrap admin registration

On first startup, `admin-service` automatically creates a bootstrap admin:

- **Email:** `admin@sms.local`

The registration code is sent by email. Locally, emails are captured by **MailHog**:

1. Open http://localhost:8025
2. Click the latest email to `admin@sms.local`
3. Note the **registration code** (6–7 digits) and **registration link**

If MailHog is empty, check the `admin-service` container logs — the code is also logged when email delivery fails:

```powershell
docker logs sms-admin
```

### Step 6 — Register the admin account

**Validate the code:**

```powershell
curl -X POST http://localhost:8080/api/auth/register/validate-code `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"admin@sms.local\",\"code\":\"YOUR_CODE_HERE\"}"
```

**Set password and complete registration:**

```powershell
curl -X POST http://localhost:8080/api/auth/register/set-password `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"admin@sms.local\",\"code\":\"YOUR_CODE_HERE\",\"password\":\"Admin@12345\"}"
```

You receive a JWT token in the response — save it for authenticated API calls.

### Step 7 — Login

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"admin@sms.local\",\"password\":\"Admin@12345\"}"
```

Use the returned token:

```
Authorization: Bearer <your-jwt-token>
```

### Step 8 — Stop the stack

```powershell
docker compose down
```

To also remove database volumes (fresh start):

```powershell
docker compose down -v
```

---

## Option B — Run in IntelliJ (with Docker for DB + email)

Use this when you want to **debug services in IntelliJ** while still using Docker for PostgreSQL and MailHog.

### Step 1 — Start only infrastructure containers

From the project root:

```powershell
cd d:\NewProject
docker compose up postgres mailhog -d
```

This starts:

| Container | Port | Purpose |
|-----------|------|---------|
| `sms-postgres` | 5432 | 6 databases (auto-created on first run) |
| `sms-mailhog` | 1025 (SMTP), 8025 (UI) | Captures registration emails |

Verify Postgres is ready:

```powershell
docker ps
```

You should see `sms-postgres` with status **healthy**.

### Step 2 — Import the project in IntelliJ

1. Open IntelliJ IDEA
2. **File → Open** → select `d:\NewProject\pom.xml`
3. Choose **Open as Project**
4. When prompted, select **Trust Project**
5. Wait for Maven to download dependencies (progress bar at bottom)

**Configure JDK:**

1. **File → Project Structure → Project**
2. Set **SDK** to **JDK 17**
3. Set **Language level** to **17**

**Enable annotation processing (for Lombok):**

1. **File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors**
2. Check **Enable annotation processing**

Install the **Lombok** plugin if prompted (**File → Settings → Plugins → search "Lombok"**).

### Step 3 — Build the project

In IntelliJ's Maven tool window (right side):

```
student-management-system → Lifecycle → clean → package
```

Or in terminal:

```powershell
cd d:\NewProject
mvn clean package -DskipTests
```

### Step 4 — Start services in order

Services must start in this order because of Eureka registration and Feign dependencies.

| Order | Main class | Module | Port |
|-------|-----------|--------|------|
| 1 | `com.sms.eureka.EurekaServerApplication` | eureka-server | 8761 |
| 2 | `com.sms.auth.AuthServiceApplication` | auth-service | 8081 |
| 3 | `com.sms.course.CourseServiceApplication` | course-service | 8085 |
| 4 | `com.sms.notification.NotificationServiceApplication` | notification-service | 8086 |
| 5 | `com.sms.student.StudentServiceApplication` | student-service | 8083 |
| 6 | `com.sms.teacher.TeacherServiceApplication` | teacher-service | 8084 |
| 7 | `com.sms.admin.AdminServiceApplication` | admin-service | 8082 |
| 8 | `com.sms.gateway.ApiGatewayApplication` | api-gateway | 8080 |

**How to run each service in IntelliJ:**

1. Open the main class file (e.g. `EurekaServerApplication.java`)
2. Click the green **Run** arrow next to `public static void main`
3. Wait until the console shows `Started ...Application`
4. Proceed to the next service

**Tip — Create Run Configurations:**

1. **Run → Edit Configurations → + → Application**
2. Name: `Eureka Server`
3. Main class: `com.sms.eureka.EurekaServerApplication`
4. Module: `eureka-server`
5. Repeat for each service above

You can then use **Run → Run...** to start each one, or use a **Compound** configuration to group them.

### Step 5 — Default local configuration

When running from IntelliJ (without extra env vars), each service uses these defaults from `application.yml`:

| Setting | Default value |
|---------|---------------|
| Database host | `localhost:5432` |
| Database user / password | `sms_user` / `sms_pass` |
| Eureka | `http://localhost:8761/eureka/` |
| Mail (SMTP) | `localhost:1025` |
| JWT secret | Built-in dev key (same across all services) |

No extra environment variables are needed if Docker Postgres and MailHog are running.

### Step 6 — Verify everything is running

1. **Eureka:** http://localhost:8761 — all 7 apps registered
2. **Gateway:** http://localhost:8080 — should not return connection refused
3. **MailHog:** http://localhost:8025 — ready to capture emails

Then follow [Step 5–7 from Option A](#step-5--complete-bootstrap-admin-registration) to register and login as admin.

---

## Option C — Fully local (PostgreSQL installed on machine)

If you do not want Docker at all, install PostgreSQL 16 locally.

### Step 1 — Install PostgreSQL

Download from https://www.postgresql.org/download/ and install. Remember the superuser password you set during installation.

### Step 2 — Create user and databases

Open **pgAdmin** or `psql` and run:

```sql
CREATE USER sms_user WITH PASSWORD 'sms_pass';
```

Then run the contents of `docker/init-databases.sql`:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE admin_db;
CREATE DATABASE student_db;
CREATE DATABASE teacher_db;
CREATE DATABASE course_db;
CREATE DATABASE notification_db;

GRANT ALL PRIVILEGES ON DATABASE auth_db TO sms_user;
GRANT ALL PRIVILEGES ON DATABASE admin_db TO sms_user;
GRANT ALL PRIVILEGES ON DATABASE student_db TO sms_user;
GRANT ALL PRIVILEGES ON DATABASE teacher_db TO sms_user;
GRANT ALL PRIVILEGES ON DATABASE course_db TO sms_user;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO sms_user;
```

### Step 3 — (Optional) Install MailHog for emails

Without MailHog, registration emails are logged to the **admin-service console** instead of being sent. To capture emails in a UI:

```powershell
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

### Step 4 — Run services in IntelliJ

Follow [Option B, Steps 2–6](#step-2--import-the-project-in-intellij).

---

## Testing the application end-to-end

### 1. Register and login as admin

See [Option A, Steps 5–7](#step-5--complete-bootstrap-admin-registration).

### 2. Create a student (admin only)

Replace `<JWT>` with your admin token:

```powershell
curl -X POST http://localhost:8080/api/admin/students `
  -H "Authorization: Bearer <JWT>" `
  -H "Content-Type: application/json" `
  -d "{\"name\":\"Jane Doe\",\"email\":\"jane@student.local\",\"phone\":\"9876543210\",\"address\":\"123 Campus Road\",\"rollNumber\":\"STU001\",\"className\":\"CS-3A\",\"department\":\"Computer Science\",\"subjects\":[\"Data Structures\",\"Algorithms\"]}"
```

Check MailHog (http://localhost:8025) for Jane's registration email and code.

### 3. Register the student

```powershell
# Validate code
curl -X POST http://localhost:8080/api/auth/register/validate-code `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"jane@student.local\",\"code\":\"STUDENT_CODE\"}"

# Set password
curl -X POST http://localhost:8080/api/auth/register/set-password `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"jane@student.local\",\"code\":\"STUDENT_CODE\",\"password\":\"Student@12345\"}"
```

### 4. View student dashboard

Login as the student, then:

```powershell
curl http://localhost:8080/api/students/dashboard `
  -H "Authorization: Bearer <STUDENT_JWT>"
```

Response includes courses, progress, and the latest notification.

### 5. View admin dashboard

```powershell
curl http://localhost:8080/api/admin/dashboard `
  -H "Authorization: Bearer <ADMIN_JWT>"
```

---

## Swagger UI (API documentation)

Each service exposes interactive API docs. When running locally:

| Service | Swagger URL |
|---------|-------------|
| Auth | http://localhost:8081/swagger-ui.html |
| Admin | http://localhost:8082/swagger-ui.html |
| Student | http://localhost:8083/swagger-ui.html |
| Teacher | http://localhost:8084/swagger-ui.html |
| Course | http://localhost:8085/swagger-ui.html |
| Notification | http://localhost:8086/swagger-ui.html |

> **Note:** Protected endpoints require a JWT. Use Postman or login first and pass `Authorization: Bearer <token>`.

---

## Port reference

Make sure these ports are free before starting:

| Port | Service |
|------|---------|
| 5432 | PostgreSQL |
| 1025 | MailHog SMTP |
| 8025 | MailHog Web UI |
| 8761 | Eureka |
| 8080 | API Gateway ← **use this for all client requests** |
| 8081 | Auth Service |
| 8082 | Admin Service |
| 8083 | Student Service |
| 8084 | Teacher Service |
| 8085 | Course Service |
| 8086 | Notification Service |

Check if a port is in use (Windows PowerShell):

```powershell
netstat -ano | findstr :8080
```

---

## Troubleshooting

### `mvn` is not recognized

Maven is not on your PATH. Either:

- Add Maven's `bin` folder to your system PATH, or
- Use IntelliJ's bundled Maven: **Maven tool window → Execute Maven Goal**

### Docker: "cannot connect to Docker API"

Docker Desktop is not running. Start Docker Desktop and wait until it is fully ready.

### Service fails with "Connection refused" to PostgreSQL

**Cause:** Postgres is not running or databases were not created.

**Fix:**

```powershell
docker compose up postgres -d
docker logs sms-postgres
```

If databases are missing, reset Postgres:

```powershell
docker compose down -v
docker compose up postgres -d
```

### Service fails with "Connection refused" to Eureka

**Cause:** Eureka is not running, or you started a service before Eureka.

**Fix:** Start `EurekaServerApplication` first. Wait until http://localhost:8761 loads, then start other services.

### "401 Unauthorized" on admin/student/teacher APIs

**Cause:** Missing or expired JWT, or calling a protected endpoint without logging in.

**Fix:**

1. Login via `POST /api/auth/login`
2. Pass the token: `Authorization: Bearer <token>`
3. Call APIs through the **gateway** at port **8080**, not directly on service ports (unless testing Swagger on a specific service)

### Registration code invalid or expired

**Cause:** Code was already used, wrong email/code pair, or code expired (valid for **7 days**).

**Fix:** Ask an admin to recreate the user (which generates a new code and email).

### No email in MailHog

**Cause:** MailHog is not running, or admin-service started before MailHog.

**Fix:**

1. Start MailHog: `docker compose up mailhog -d`
2. Restart `admin-service`
3. Check admin-service logs — the registration code is logged if email fails:

   ```
   Failed to send email to ... Registration link: ..., code: ...
   ```

### Port already in use

**Cause:** Another application is using the same port.

**Fix:** Stop the conflicting process or change the port in the service's `application.yml` under `server.port`.

### IntelliJ: "Cannot resolve symbol" for Lombok

**Fix:**

1. Install the Lombok plugin
2. Enable annotation processing (see Option B, Step 2)
3. **File → Invalidate Caches → Invalidate and Restart**

### IntelliJ: Maven dependencies not downloading

**Fix:**

1. Right-click root `pom.xml` → **Maven → Reload Project**
2. Check internet connection and proxy settings in **File → Settings → HTTP Proxy**

### Docker build is very slow or fails on Windows

**Fix:**

1. Ensure Docker Desktop uses **Linux containers** (default)
2. Allocate more memory: **Docker Desktop → Settings → Resources → Memory** (至少 4 GB recommended)
3. Run build again: `docker compose up --build`

---

## Quick checklist

Use this before testing:

- [ ] Docker Desktop is running (if using Docker)
- [ ] PostgreSQL is up and 6 databases exist
- [ ] MailHog is running on port 8025 (optional but recommended)
- [ ] Eureka is running at http://localhost:8761
- [ ] All 7 microservices show as **UP** in Eureka
- [ ] API Gateway is running at http://localhost:8080
- [ ] Bootstrap admin registered (`admin@sms.local`)
- [ ] Admin login returns a JWT token

---

## Next steps

- Explore APIs via Swagger (links above)
- Create teachers and students from the admin dashboard API
- Review the main [README.md](../README.md) for architecture and GCP deployment notes
