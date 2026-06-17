# Production Deployment Guide — Student Management System

This guide describes how to take the SMS microservices application from local development to a **real-world production deployment**. It covers infrastructure, hosting options, security, monitoring, scaling, and operational best practices.

> **Local development:** see [LOCAL_SETUP.md](LOCAL_SETUP.md)  
> **GCP step-by-step:** see [GCP_DEPLOYMENT.md](GCP_DEPLOYMENT.md)  
> **Project overview:** see [../README.md](../README.md)

---

## Table of contents

1. [Production architecture](#1-production-architecture)
2. [Components you need](#2-components-you-need)
3. [Hosting options](#3-hosting-options)
4. [Pre-deployment checklist](#4-pre-deployment-checklist)
5. [Secrets and environment variables](#5-secrets-and-environment-variables)
6. [Database setup](#6-database-setup)
7. [Backend deployment](#7-backend-deployment)
8. [Frontend deployment](#8-frontend-deployment)
9. [Email configuration](#9-email-configuration)
10. [Security measures](#10-security-measures)
11. [Monitoring and observability](#11-monitoring-and-observability)
12. [Scaling strategies](#12-scaling-strategies)
13. [CI/CD pipeline](#13-cicd-pipeline)
14. [Reverse proxy and TLS (Nginx example)](#14-reverse-proxy-and-tls-nginx-example)
15. [Post-deployment verification](#15-post-deployment-verification)
16. [Backup and disaster recovery](#16-backup-and-disaster-recovery)
17. [Best practices summary](#17-best-practices-summary)
18. [Recommended evolution path](#18-recommended-evolution-path)

---

## 1. Production architecture

### Current application stack

```
Browser (React SPA)
        │
        ▼
┌───────────────────────────────────────────────────────────┐
│  HTTPS Load Balancer / Reverse Proxy (Nginx, ALB, etc.)   │
│    • https://app.yourdomain.com   → React static files     │
│    • https://api.yourdomain.com   → API Gateway only      │
└───────────────────────────────────────────────────────────┘
        │
        ▼
API Gateway (8080) — JWT validation, CORS, routing
        │
        ▼
Eureka Server (8761) — service discovery (internal only)
        │
        ├── auth-service (8081)         → auth_db
        ├── admin-service (8082)        → admin_db
        ├── student-service (8083)      → student_db
        ├── teacher-service (8084)      → teacher_db
        ├── course-service (8085)       → course_db
        └── notification-service (8086) → notification_db
                    │
                    ▼
        Managed PostgreSQL (6 databases)
                    │
                    ▼
        Real SMTP provider (SendGrid, SES, etc.)
```

### What must be public vs. internal

| Component | Public internet? | Notes |
|-----------|------------------|-------|
| React frontend (`frontend/dist`) | **Yes** | Static files behind HTTPS |
| API Gateway (`8080`) | **Yes** | Single API entry point |
| Eureka (`8761`) | **No** | Internal service mesh only |
| Individual microservices (`8081–8086`) | **No** | Reachable only inside private network |
| PostgreSQL (`5432`) | **No** | Use private network / managed DB |
| MailHog | **No** | Dev only — remove in production |
| Swagger UI (`/swagger-ui.html`) | **No** | Disable in production |

---

## 2. Components you need

### Required

| Component | Purpose |
|-----------|---------|
| **Compute** | Run Docker containers or JVM processes |
| **Container registry** | Store built images (Docker Hub, ECR, GCR, ACR) |
| **PostgreSQL** | Six databases: `auth_db`, `admin_db`, `student_db`, `teacher_db`, `course_db`, `notification_db` |
| **Domain name** | e.g. `yourdomain.com`, `api.yourdomain.com` |
| **TLS certificate** | HTTPS (Let's Encrypt, cloud LB, Cloudflare) |
| **Secrets store** | JWT secret, DB passwords, encryption key, SMTP credentials |
| **SMTP provider** | Registration and invitation emails |
| **Static file hosting** | Serve the React production build |
| **Reverse proxy / load balancer** | Route traffic, terminate TLS |

### Strongly recommended

| Component | Purpose |
|-----------|---------|
| **CI/CD** | Automated build, test, deploy |
| **Centralized logging** | Aggregate logs from all services |
| **Metrics & alerting** | Uptime, latency, error rates |
| **Database backups** | Automated daily backups with retention |
| **WAF / rate limiting** | Protect login and public APIs |
| **DB migration tool** | Flyway or Liquibase (replace `ddl-auto: update`) |

### Optional (at scale)

| Component | Purpose |
|-----------|---------|
| **Kubernetes** | Orchestration when running many replicas |
| **Redis** | Distributed rate limiting / session cache |
| **Message queue** | Async email and notifications (RabbitMQ, SQS) |
| **CDN** | Faster frontend delivery globally |
| **APM** | Distributed tracing (Datadog, New Relic, Jaeger) |

---

## 3. Hosting options

### Option A — Single VPS (simplest, good for MVP)

**Examples:** DigitalOcean Droplet, Linode, Hetzner, AWS EC2

| Pros | Cons |
|------|------|
| Low cost, easy to understand | Single point of failure |
| Docker Compose works with minimal changes | Manual scaling |
| Fast to deploy | You manage OS patches and backups |

**Suggested size:** 4 GB RAM minimum (8 GB recommended for all 9 services + Postgres)

```
VPS
 ├── Docker Compose (all microservices)
 ├── Nginx (TLS + routing)
 ├── PostgreSQL (or connect to managed DB)
 └── frontend/dist served by Nginx
```

---

### Option B — Managed containers (balanced)

**Examples:** AWS ECS/Fargate, Google Cloud Run, Azure Container Apps

| Pros | Cons |
|------|------|
| No server management | More cloud-specific config |
| Auto-restart, health checks | Higher cost than a single VPS |
| Integrates with managed DB | Learning curve |

**Pattern:**
- One container per microservice
- Managed RDS / Cloud SQL for Postgres
- ALB / Cloud Load Balancing in front of gateway
- S3 + CloudFront or Firebase Hosting for frontend

---

### Option C — Kubernetes (best for scale)

**Examples:** EKS, GKE, AKS, self-hosted K8s

| Pros | Cons |
|------|------|
| Horizontal scaling per service | Operational complexity |
| Rolling updates, self-healing | Requires K8s expertise |
| Industry standard at scale | Higher baseline cost |

**Pattern:**
- Deployments per service
- Ingress for gateway + frontend
- Helm charts for environment config
- Consider replacing Eureka with Kubernetes DNS + Spring Cloud Kubernetes

---

### Option D — Hybrid (recommended for many teams)

| Layer | Service |
|-------|---------|
| Frontend | Vercel, Netlify, S3+CloudFront, or Nginx on VPS |
| API | API Gateway on ECS/VM behind load balancer |
| Databases | Managed PostgreSQL (RDS, Cloud SQL, Supabase) |
| Email | SendGrid / AWS SES |
| Secrets | AWS Secrets Manager / HashiCorp Vault |

---

## 4. Pre-deployment checklist

Complete these before going live:

### Code and configuration

- [ ] Replace all dev defaults in `docker-compose.yml` (JWT, DB password, encryption key)
- [ ] Set `REGISTRATION_BASE_URL` to production frontend URL
- [ ] Set `app.security.allowed-origins` on API Gateway to production frontend origin(s)
- [ ] Remove or disable MailHog; configure real SMTP
- [ ] Build frontend with `VITE_API_BASE_URL` pointing to production API
- [ ] Disable Swagger / OpenAPI in production profiles
- [ ] Add Flyway/Liquibase migrations (stop relying on `ddl-auto: update`)
- [ ] Run `mvn test` and frontend build successfully
- [ ] Review CORS — only allow your real domain(s)

### Infrastructure

- [ ] Provision compute and networking
- [ ] Create PostgreSQL instance and six databases
- [ ] Configure DNS records (`A` / `CNAME` for app and api subdomains)
- [ ] Obtain and install TLS certificates
- [ ] Configure firewall: only ports 80 and 443 public
- [ ] Set up secrets manager (no secrets in git or plain env files on disk)

### Operations

- [ ] Configure log aggregation
- [ ] Set up health check monitoring and alerts
- [ ] Enable automated database backups
- [ ] Document rollback procedure
- [ ] Test bootstrap admin registration flow end-to-end
- [ ] Test student/teacher creation and email delivery

---

## 5. Secrets and environment variables

### Critical secrets (never commit to git)

| Variable | Services | Description |
|----------|----------|-------------|
| `JWT_SECRET` | `auth-service`, `api-gateway` | Must be identical; use 256+ bit random string |
| `APP_ENCRYPTION_KEY` | `admin-service`, `student-service`, `teacher-service` | AES key for encrypted phone/address fields |
| `DB_USER` / `DB_PASSWORD` | All services with a database | Strong, unique credentials |
| `MAIL_*` / SMTP credentials | `admin-service` | Real email provider settings |

### Application configuration

| Variable | Service | Production example |
|----------|---------|-------------------|
| `REGISTRATION_BASE_URL` | `auth-service` | `https://app.yourdomain.com/register` |
| `EUREKA_HOST` | All backend services | `eureka-server` (Docker network) or K8s service name |
| `DB_HOST` | All DB services | Managed DB hostname |
| `MAIL_HOST` | `admin-service` | `smtp.sendgrid.net` |
| `MAIL_PORT` | `admin-service` | `587` |
| `MAIL_FROM` | `admin-service` | `noreply@yourdomain.com` |
| `app.security.allowed-origins` | `api-gateway` | `https://app.yourdomain.com` |

### Frontend build-time variable

| Variable | Build command | Production example |
|----------|---------------|-------------------|
| `VITE_API_BASE_URL` | `npm run build` | `https://api.yourdomain.com` |

```bash
# Example production frontend build
cd frontend
VITE_API_BASE_URL=https://api.yourdomain.com npm run build
```

### Secrets management approaches

1. **Cloud secrets manager** — inject at deploy time (recommended)
2. **Docker secrets / K8s secrets** — mounted as env vars or files
3. **`.env` file on server** — acceptable for small VPS; restrict file permissions (`chmod 600`)

**Never** use dev values in production:

```
JWT_SECRET=StudentManagementSystemSecretKeyForJWT2024MustBeLongEnough   # ❌ dev default
APP_ENCRYPTION_KEY=LocalDevEncryptionKeyChangeInProd                    # ❌ dev default
DB_PASSWORD=sms_pass                                                    # ❌ dev default
```

---

## 6. Database setup

### Databases required

The application uses **six separate PostgreSQL databases** (see `docker/init-databases.sql`):

```sql
CREATE DATABASE auth_db;
CREATE DATABASE admin_db;
CREATE DATABASE student_db;
CREATE DATABASE teacher_db;
CREATE DATABASE course_db;
CREATE DATABASE notification_db;
```

### Production recommendations

| Topic | Recommendation |
|-------|----------------|
| **Hosting** | Managed PostgreSQL (AWS RDS, Cloud SQL, Azure Database) |
| **Version** | PostgreSQL 16 (matches local dev) |
| **HA** | Multi-AZ / read replicas for production workloads |
| **Connections** | Use connection pooling (PgBouncer) if scaling replicas |
| **Schema changes** | Use Flyway/Liquibase — avoid `ddl-auto: update` in prod |
| **Encryption** | Enable encryption at rest (managed DB default) |
| **Network** | Private subnet only; no public `5432` |

### Schema migration note

Local development uses Hibernate `ddl-auto: update`. For production:

1. Add Flyway migration scripts under `src/main/resources/db/migration/`
2. Set `spring.jpa.hibernate.ddl-auto: validate` in a `prod` Spring profile
3. Run migrations as part of CI/CD before or during deploy

For existing volumes upgraded from older versions, see `docker/migrations/001-auth-user-lockout.sql`.

---

## 7. Backend deployment

### Build Docker images

Each service uses the root `Dockerfile` with a `MODULE` build argument:

```bash
# Build all images (example)
docker build --build-arg MODULE=api-gateway -t sms/api-gateway:latest .
docker build --build-arg MODULE=auth-service -t sms/auth-service:latest .
docker build --build-arg MODULE=admin-service -t sms/admin-service:latest .
docker build --build-arg MODULE=student-service -t sms/student-service:latest .
docker build --build-arg MODULE=teacher-service -t sms/teacher-service:latest .
docker build --build-arg MODULE=course-service -t sms/course-service:latest .
docker build --build-arg MODULE=notification-service -t sms/notification-service:latest .
docker build --build-arg MODULE=eureka-server -t sms/eureka-server:latest .
```

Push images to your container registry before deploying to cloud/container platforms.

### Startup order

Services have dependencies. Start in this order:

1. PostgreSQL (all databases ready)
2. `eureka-server`
3. `auth-service`, `course-service`, `notification-service`
4. `student-service`, `teacher-service`
5. `admin-service` (requires auth + other services + SMTP)
6. `api-gateway` (last — depends on all registered services)

Docker Compose `depends_on` handles this locally. In Kubernetes, use init containers or readiness probes.

### Production `docker-compose` changes

Create a separate `docker-compose.prod.yml` (do not use dev compose as-is):

- Remove `mailhog` service
- Remove public port mappings for internal services (`8081–8086`, `8761`, `5432`)
- Expose only `api-gateway` (or nothing if Nginx is on the same host)
- Use external managed Postgres or a hardened Postgres with no public port
- Set `restart: unless-stopped` on all services
- Inject secrets from environment or secrets files

### Spring production profile (recommended addition)

Create `application-prod.yml` per service with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

logging:
  level:
    root: INFO
```

Activate with `SPRING_PROFILES_ACTIVE=prod`.

---

## 8. Frontend deployment

The React app is a **static SPA** built with Vite.

### Build

```bash
cd frontend
npm ci
VITE_API_BASE_URL=https://api.yourdomain.com npm run build
```

Output: `frontend/dist/` (HTML, JS, CSS, assets)

### Hosting options

| Platform | Notes |
|----------|-------|
| **Nginx on VPS** | Serve `dist/` alongside reverse proxy config |
| **AWS S3 + CloudFront** | Cheap, scalable, add CDN |
| **Netlify / Vercel** | Simple CI from git, automatic HTTPS |
| **Firebase Hosting** | Good for static SPAs |

### SPA routing

Configure your host to return `index.html` for all non-file routes (React Router):

```nginx
location / {
    root /var/www/sms-frontend;
    try_files $uri $uri/ /index.html;
}
```

### CORS

When `VITE_API_BASE_URL` points to a different origin (e.g. `api.yourdomain.com`), the API Gateway must allow the frontend origin in `app.security.allowed-origins`.

---

## 9. Email configuration

Local development uses **MailHog** (fake SMTP). Production requires a real provider.

### Replace MailHog

Remove MailHog from production compose and configure `admin-service`:

```yaml
environment:
  MAIL_HOST: smtp.sendgrid.net      # provider-specific
  MAIL_PORT: 587
  MAIL_FROM: noreply@yourdomain.com
```

### Provider examples

| Provider | SMTP host | Port |
|----------|-----------|------|
| SendGrid | `smtp.sendgrid.net` | 587 |
| AWS SES | `email-smtp.<region>.amazonaws.com` | 587 |
| Mailgun | `smtp.mailgun.org` | 587 |
| Gmail (not recommended for prod) | `smtp.gmail.com` | 587 |

### DNS records for deliverability

- **SPF** — authorize your SMTP server to send on behalf of your domain
- **DKIM** — cryptographic signature (provider gives you DNS records)
- **DMARC** — policy for failed authentication

### Registration link

Set `REGISTRATION_BASE_URL` so emails contain the correct production link:

```
https://app.yourdomain.com/register
```

---

## 10. Security measures

### Already implemented in this project

| Feature | Location |
|---------|----------|
| JWT authentication | API Gateway + Auth service |
| Role-based path authorization | `JwtAuthenticationFilter` (ADMIN / STUDENT / TEACHER) |
| Login lockout | `failedLoginAttempts`, `accountLockedUntil` on User |
| Verification throttling | Auth service registration code attempts |
| Input sanitization | `InputSanitizer` across services |
| Sensitive field encryption | `SensitiveStringEncryptor` (phone, address) |
| DTO validation | `common-lib` request DTOs |
| Standardized error responses | `GlobalExceptionHandler`, `ApiResponse` |
| CORS restrictions | `CorsConfig` on API Gateway |
| Security headers | Auth `SecurityConfig` |

### Production hardening checklist

| Area | Action |
|------|--------|
| **TLS** | HTTPS everywhere; redirect HTTP → HTTPS |
| **Secrets** | Rotate JWT and encryption keys; use secrets manager |
| **Network** | Private VPC; only gateway and frontend public |
| **Database** | No public access; least-privilege DB users per service |
| **Swagger** | Disable in production (`springdoc` off) |
| **Eureka** | Internal only; do not expose port 8761 |
| **CORS** | Allow only your production frontend origin(s) |
| **Rate limiting** | Add at gateway (Spring Cloud Gateway rate limiter or WAF) |
| **Headers** | HSTS, X-Content-Type-Options, X-Frame-Options via proxy |
| **Dependencies** | Regular `mvn dependency:check` / Dependabot |
| **Container images** | Scan for CVEs; use minimal base images (already JRE Alpine) |
| **Audit logging** | Log admin actions (user creation, deletes) |
| **Token expiry** | Review `jwt.expiration-ms` (default 24h) |

### JWT secret requirements

- Minimum 256 bits of entropy
- Same value in `auth-service` and `api-gateway`
- Rotate periodically with a planned token invalidation strategy

---

## 11. Monitoring and observability

### Health checks

| Service | Endpoint | Notes |
|---------|----------|-------|
| Eureka | `/actuator/health` or `/` | Used in Docker healthcheck |
| API Gateway | `/actuator/health` | Add Spring Boot Actuator if not present |
| Other services | `/actuator/health` | Recommend adding Actuator to all services |

### What to monitor

| Metric | Alert threshold (example) |
|--------|---------------------------|
| API Gateway uptime | < 99.9% |
| HTTP 5xx rate | > 1% over 5 minutes |
| Login failure spike | Possible brute force |
| DB connection pool exhaustion | > 80% utilization |
| Disk / memory on VPS | > 85% |
| Email send failures | Any sustained failures |
| Eureka registration | Service deregistered unexpectedly |

### Logging

| Approach | Tools |
|----------|-------|
| **VPS / Docker** | Loki + Grafana, ELK stack, or `docker logs` → centralized agent |
| **Cloud** | CloudWatch, Google Cloud Logging, Azure Monitor |
| **SaaS** | Datadog, Splunk, Better Stack |

**Structured JSON logs** (Logback JSON encoder) make searching across microservices much easier.

### Recommended log fields

- `timestamp`, `service`, `level`, `message`
- `requestId` / `traceId` (correlate across gateway → downstream)
- `userEmail` / `role` (for audit, not passwords)

### Uptime monitoring

External probes (UptimeRobot, Pingdom, Better Uptime):

- `GET https://app.yourdomain.com` — frontend loads
- `GET https://api.yourdomain.com/actuator/health` — API healthy
- Optional: synthetic login test on a schedule

### Distributed tracing (optional, recommended at scale)

- Spring Cloud Sleuth / Micrometer Tracing + Zipkin or Jaeger
- Trace requests from gateway through Feign calls to downstream services

---

## 12. Scaling strategies

### Vertical scaling (simplest)

Increase CPU/RAM on the VPS or container size. Good first step when response times degrade.

### Horizontal scaling (per service)

| Service | Scale priority | Notes |
|---------|----------------|-------|
| `api-gateway` | **High** | Stateless; scale behind load balancer |
| `auth-service` | **High** | Login traffic hotspot |
| `student-service` / `teacher-service` | Medium | Read-heavy dashboards |
| `admin-service` | Low–Medium | Admin-only traffic |
| `course-service` / `notification-service` | Low | Typically lighter load |
| `eureka-server` | Low | Usually 1–2 instances; consider HA pair |

**Requirements for horizontal scaling:**
- Stateless services (already true — JWT, no server sessions)
- Shared PostgreSQL (managed DB)
- Multiple gateway instances behind a load balancer
- Eureka sees all instances (or migrate to K8s service discovery)

### Database scaling

| Strategy | When |
|----------|------|
| Connection pooling (PgBouncer) | Many service replicas |
| Read replicas | Heavy read dashboards |
| Separate DB per service | Already implemented (6 DBs) |
| Caching (Redis) | Frequently read course/notification lists |

### Autoscaling triggers (Kubernetes / cloud)

- CPU > 70% for 5 minutes → add replica
- Request latency p95 > 500ms → add gateway/auth replicas
- Scale down during off-peak hours to save cost

---

## 13. CI/CD pipeline

### Recommended pipeline stages

```
┌─────────┐   ┌─────────┐   ┌──────────────┐   ┌─────────┐   ┌─────────────┐
│  Lint   │ → │  Test   │ → │ Build images │ → │  Push   │ → │   Deploy    │
│         │   │ mvn test│   │ docker build │   │ registry│   │ staging/prod│
└─────────┘   └─────────┘   └──────────────┘   └─────────┘   └─────────────┘
                                    │
                              npm run build
                              (frontend)
```

### Example GitHub Actions jobs

1. **Backend CI** — `mvn clean verify` on every PR
2. **Frontend CI** — `npm ci && npm run build` on every PR
3. **Docker build** — build and push images on merge to `main`
4. **Deploy staging** — auto-deploy to staging environment
5. **Deploy production** — manual approval gate

### Deployment strategies

| Strategy | Risk | Downtime |
|----------|------|----------|
| **Rolling update** | Low | None |
| **Blue-green** | Very low | Brief switchover |
| **Canary** | Lowest | None |

Start with rolling updates via Docker Compose or Kubernetes `RollingUpdate`.

### Environment promotion

```
dev (local) → staging (cloud, prod-like) → production
```

Staging should use the same secrets structure (different values) and real SMTP in test mode.

---

## 14. Reverse proxy and TLS (Nginx example)

Typical VPS setup with Nginx terminating TLS:

```nginx
# /etc/nginx/sites-available/sms

# Frontend
server {
    listen 443 ssl http2;
    server_name app.yourdomain.com;

    ssl_certificate     /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    root /var/www/sms-frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}

# API Gateway
server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate     /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Redirect HTTP → HTTPS
server {
    listen 80;
    server_name app.yourdomain.com api.yourdomain.com;
    return 301 https://$host$request_uri;
}
```

Obtain certificates with **Certbot**:

```bash
sudo certbot --nginx -d app.yourdomain.com -d api.yourdomain.com
```

---

## 15. Post-deployment verification

Run through this checklist after every production deploy:

### Infrastructure

- [ ] `https://app.yourdomain.com` loads the React app
- [ ] `https://api.yourdomain.com` responds (not 404 for `/api/auth/login` POST)
- [ ] Eureka shows all services registered (internal check)
- [ ] TLS certificate valid and auto-renewing

### Authentication flow

- [ ] Bootstrap admin registration works (code email received)
- [ ] Admin login returns JWT
- [ ] Invalid credentials return 401 (not 500)
- [ ] Locked account message appears after repeated failures

### Admin operations

- [ ] Create student → registration email delivered
- [ ] Create teacher → registration email delivered
- [ ] Student/teacher can register and login
- [ ] Role-based access enforced (student cannot access `/api/admin/**`)

### Data and security

- [ ] Phone/address stored encrypted in DB (not plaintext)
- [ ] Swagger UI not accessible publicly
- [ ] Internal ports not reachable from internet
- [ ] CORS rejects unknown origins

### Smoke test commands

```bash
# Health (if actuator enabled)
curl -s https://api.yourdomain.com/actuator/health

# Login
curl -s -X POST https://api.yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@yourdomain.com","password":"YOUR_PASSWORD"}'
```

---

## 16. Backup and disaster recovery

### Database backups

| Item | Recommendation |
|------|----------------|
| **Frequency** | Daily automated backups |
| **Retention** | 30 days minimum |
| **Testing** | Monthly restore drill |
| **Storage** | Separate region/account from production |

Managed PostgreSQL (RDS, Cloud SQL) provides point-in-time recovery — enable it.

### Application state

- Microservices are stateless (JWT) — redeploy from images
- Secrets: backed up in secrets manager
- Frontend: rebuild from git at any time

### Recovery procedure (outline)

1. Provision new infrastructure
2. Restore PostgreSQL from latest backup
3. Deploy latest container images
4. Restore secrets from secrets manager
5. Verify health checks and run smoke tests
6. Update DNS if endpoints changed

### RTO / RPO targets (define for your org)

| Metric | Small deployment target |
|--------|-------------------------|
| **RPO** (max data loss) | 24 hours (daily backup) |
| **RTO** (time to restore) | 2–4 hours |

---

## 17. Best practices summary

### Do

- Use managed PostgreSQL and real SMTP
- Keep only gateway + frontend public
- Store secrets in a secrets manager
- Use HTTPS everywhere
- Disable Swagger in production
- Add Flyway migrations before first prod deploy
- Monitor health, errors, and login failures
- Automate builds and deployments
- Test registration and email flows in staging
- Run `docker compose` locally to validate images before pushing

### Don't

- Expose Eureka, individual services, or Postgres to the internet
- Use MailHog, dev JWT secrets, or default DB passwords in production
- Commit `.env` files with real secrets
- Rely on `ddl-auto: update` for schema changes in production
- Skip backups because "it's a small app"
- Deploy frontend with `VITE_API_BASE_URL` pointing to `localhost`

---

## 18. Recommended evolution path

A practical roadmap from where the project is today to a mature production system:

### Phase 1 — MVP launch (1–2 weeks)

- Single VPS or small cloud VM
- Docker Compose production file
- Managed Postgres or hardened local Postgres
- Nginx + Let's Encrypt
- Real SMTP (SendGrid)
- Frontend on same VPS or Netlify
- Manual deploy from git

### Phase 2 — Operational maturity (1–2 months)

- CI/CD (GitHub Actions)
- Flyway migrations, `prod` Spring profile
- Centralized logging
- Uptime monitoring and alerts
- Automated DB backups
- Staging environment

### Phase 3 — Scale and resilience (3–6 months)

- Move to Kubernetes or managed containers
- Horizontal scaling for gateway + auth
- Redis for rate limiting
- Distributed tracing
- WAF / DDoS protection
- Replace Eureka with K8s-native discovery (optional)
- Async email via message queue

---

## Quick reference — environment variable matrix

| Variable | auth | gateway | admin | student | teacher | course | notification | eureka |
|----------|:----:|:-------:|:-----:|:-------:|:-------:|:------:|:------------:|:------:|
| `DB_HOST` | ✓ | | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `DB_NAME` | ✓ | | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `DB_USER` | ✓ | | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `DB_PASSWORD` | ✓ | | ✓ | ✓ | ✓ | ✓ | ✓ | |
| `EUREKA_HOST` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| `JWT_SECRET` | ✓ | ✓ | | | | | | |
| `REGISTRATION_BASE_URL` | ✓ | | | | | | | |
| `APP_ENCRYPTION_KEY` | | | ✓ | ✓ | ✓ | | | |
| `MAIL_HOST` | | | ✓ | | | | | |
| `MAIL_PORT` | | | ✓ | | | | | |
| `MAIL_FROM` | | | ✓ | | | | | |
| `app.security.allowed-origins` | | ✓ | | | | | | |

---

## Related documentation

- [LOCAL_SETUP.md](LOCAL_SETUP.md) — run the stack locally
- [../frontend/README.md](../frontend/README.md) — frontend development
- [../docker/migrations/001-auth-user-lockout.sql](../docker/migrations/001-auth-user-lockout.sql) — DB migration for existing auth volumes
- [../README.md](../README.md) — architecture and API overview

---

*Last updated: June 2026*
