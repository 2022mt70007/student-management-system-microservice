# Student Management System — Architecture Diagrams

Visual architecture for reports, viva, and documentation.  
Render at [mermaid.live](https://mermaid.live) or any Markdown viewer that supports Mermaid (GitHub, GitLab, VS Code).

> **Related:** [LOCAL_SETUP.md](LOCAL_SETUP.md) · [PRODUCTION_DEPLOYMENT.md](PRODUCTION_DEPLOYMENT.md) · [GCP_DEPLOYMENT.md](GCP_DEPLOYMENT.md)

---

## 1. High-level layered architecture

```mermaid
flowchart TB
    subgraph CLIENT["Client Layer"]
        Browser["Web Browser"]
        React["React 19 + TypeScript + Vite<br/>:5173<br/>Login · Register · Dashboards"]
        Browser --> React
    end

    subgraph GATEWAY["API Gateway Layer — :8080"]
        GW["Spring Cloud Gateway<br/>sms-gateway"]
        JWT["JWT Validation"]
        RBAC["Role-Based Access Control<br/>ADMIN · STUDENT · TEACHER"]
        CORS["CORS Policy"]
        GW --> JWT
        GW --> RBAC
        GW --> CORS
    end

    subgraph DISCOVERY["Service Discovery — :8761"]
        Eureka["Netflix Eureka Server<br/>sms-eureka<br/>(internal only in production)"]
    end

    subgraph SERVICES["Microservices Layer — Spring Boot 3.2.5 / Java 17"]
        Auth["auth-service<br/>:8081"]
        Admin["admin-service<br/>:8082"]
        Student["student-service<br/>:8083"]
        Teacher["teacher-service<br/>:8084"]
        Course["course-service<br/>:8085"]
        Notification["notification-service<br/>:8086"]
    end

    subgraph DATA["Database Layer — PostgreSQL 16 :5432"]
        DB[(sms-postgres)]
        AuthDB[(auth_db)]
        AdminDB[(admin_db)]
        StudentDB[(student_db)]
        TeacherDB[(teacher_db)]
        CourseDB[(course_db)]
        NotifDB[(notification_db)]
        DB --> AuthDB
        DB --> AdminDB
        DB --> StudentDB
        DB --> TeacherDB
        DB --> CourseDB
        DB --> NotifDB
    end

    subgraph EMAIL["Email Delivery"]
        SES["AWS SES SMTP<br/>email-smtp.ap-south-1.amazonaws.com:587<br/>(production / demo)"]
        MailHog["MailHog<br/>:1025 / UI :8025<br/>(local dev only)"]
    end

    subgraph SHARED["Shared Library"]
        CommonLib["common-lib<br/>DTOs · JWT util · InputSanitizer<br/>SensitiveStringEncryptor · EmailValidator"]
    end

    React -->|"HTTPS/HTTP REST<br/>/api/* + Bearer JWT"| GW

    GW -->|"/api/auth/**"| Auth
    GW -->|"/api/admin/**"| Admin
    GW -->|"/api/students/**"| Student
    GW -->|"/api/teachers/**"| Teacher
    GW -->|"/api/courses/**"| Course
    GW -->|"/api/notifications/**"| Notification

    Auth & Admin & Student & Teacher & Course & Notification -.->|register / discover| Eureka
    GW -.-> Eureka

    Admin -->|"OpenFeign"| Auth
    Admin -->|"OpenFeign"| Student
    Admin -->|"OpenFeign"| Teacher
    Admin -->|"OpenFeign"| Course
    Admin -->|"OpenFeign"| Notification

    Auth --> AuthDB
    Admin --> AdminDB
    Student --> StudentDB
    Teacher --> TeacherDB
    Course --> CourseDB
    Notification --> NotifDB

    Admin -->|"SMTP TLS"| SES
    Admin -.->|"SMTP (dev)"| MailHog

    Auth & Admin & Student & Teacher & Course & Notification -.-> CommonLib

    classDef client fill:#dbeafe,stroke:#2563eb,color:#1e3a8a
    classDef gateway fill:#ffedd5,stroke:#ea580c,color:#7c2d12
    classDef service fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef data fill:#f3e8ff,stroke:#9333ea,color:#581c87
    classDef external fill:#f3f4f6,stroke:#6b7280,color:#374151
    classDef shared fill:#fef9c3,stroke:#ca8a04,color:#713f12

    class Browser,React client
    class GW,JWT,RBAC,CORS gateway
    class Auth,Admin,Student,Teacher,Course,Notification service
    class DB,AuthDB,AdminDB,StudentDB,TeacherDB,CourseDB,NotifDB data
    class SES,MailHog,Eureka external
    class CommonLib shared
```

---

## 2. Registration & email flow (sequence)

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Admin User
    participant UI as React Admin Portal<br/>:5173
    participant GW as API Gateway<br/>:8080
    participant ADM as admin-service<br/>:8082
    participant STU as student-service<br/>:8083
    participant AUTH as auth-service<br/>:8081
    participant DB as registration_invitations<br/>(auth_db)
    participant SES as AWS SES SMTP
    actor NewUser as New Student

    Admin->>UI: Create student (name, email, ...)
    UI->>GW: POST /api/admin/students + JWT
    GW->>GW: Validate JWT & ADMIN role
    GW->>ADM: Forward request

    ADM->>ADM: EmailValidator (format + domain)
    ADM->>STU: Feign: create student profile
    STU-->>ADM: StudentResponse (PENDING_REGISTRATION)

    ADM->>AUTH: Feign: POST /api/auth/invitations
    AUTH->>DB: Save email, code, role, profileId, expiresAt
    AUTH-->>ADM: InvitationResponse (code + link)

    ADM->>SES: SMTP send registration email
    alt Email sent successfully
        SES-->>NewUser: Email with link + code
        ADM-->>UI: 200 Student created. Email sent.
    else SES error (unverified / invalid)
        SES-->>ADM: SMTP error
        ADM-->>UI: 400 Clear error + registration code
    end

    NewUser->>UI: Open /register
    UI->>GW: POST /api/auth/register/validate-code
    GW->>AUTH: Validate code
    AUTH->>DB: Check code, expiry, used=false
    AUTH-->>UI: Code valid

    NewUser->>UI: Set password
    UI->>GW: POST /api/auth/register/set-password
    AUTH->>DB: Mark invitation used
    AUTH->>AUTH: Create user (BCrypt password)
    AUTH->>STU: Activate profile (ACTIVE)
    AUTH-->>UI: JWT token

    NewUser->>UI: Login → Student Dashboard
```

---

## 3. Security architecture

```mermaid
flowchart LR
    subgraph PERIMETER["Perimeter"]
        CORS2["CORS<br/>Allowed origins only"]
        GW2["API Gateway<br/>Single entry point"]
    end

    subgraph AUTHZ["Authentication & Authorization"]
        JWT2["JWT Tokens<br/>24h expiry"]
        RBAC2["Path-based RBAC<br/>ADMIN / STUDENT / TEACHER"]
        BCrypt["BCrypt Password Hashing"]
    end

    subgraph REG["Registration Security"]
        INV["Invitation codes<br/>6-7 digits · 7-day expiry · one-time use"]
        THROTTLE["Verification throttling"]
        LOCK["Login lockout<br/>failed attempts"]
    end

    subgraph INPUT["Input & Data Protection"]
        VAL["Jakarta Bean Validation<br/>DTO constraints"]
        SAN["InputSanitizer"]
        ENC["AES-GCM Encryption<br/>phone · address fields"]
    end

    subgraph ERR["Error Handling"]
        GEH["GlobalExceptionHandler<br/>all services"]
        API["ApiResponse<br/>no stack traces to client"]
    end

    subgraph SECRETS["Secrets Management"]
        ENV["Environment variables<br/>JWT_SECRET · APP_ENCRYPTION_KEY<br/>SES SMTP credentials (.env)"]
    end

    Client2["Client"] --> CORS2 --> GW2
    GW2 --> JWT2 --> RBAC2
    AUTH2["auth-service"] --> BCrypt
    AUTH2 --> INV --> THROTTLE --> LOCK
    Services["All services"] --> VAL --> SAN
    StudentTeacherAdmin["student · teacher · admin"] --> ENC
    Services --> GEH --> API
    Services --> ENV

    classDef sec fill:#fef3c7,stroke:#d97706,color:#78350f
    class CORS2,GW2,JWT2,RBAC2,BCrypt,INV,THROTTLE,LOCK,VAL,SAN,ENC,GEH,API,ENV sec
```

---

## 4. Docker Compose deployment (local / demo)

```mermaid
flowchart TB
    subgraph HOST["Developer Machine / VM"]
        subgraph DOCKER["Docker Compose Network"]
            PG["sms-postgres<br/>PostgreSQL 16 :5432<br/>6 databases"]
            MH["sms-mailhog<br/>:1025 / :8025"]
            EU["sms-eureka<br/>:8761"]
            AS["sms-auth :8081"]
            AD["sms-admin :8082"]
            SS["sms-student :8083"]
            TS["sms-teacher :8084"]
            CS["sms-course :8085"]
            NS["sms-notification :8086"]
            GWC["sms-gateway :8080"]
        end

        FE["npm run dev<br/>React frontend :5173<br/>(host process)"]
    end

    subgraph AWS["AWS Cloud"]
        SES2["AWS SES<br/>ap-south-1<br/>SMTP :587"]
    end

    FE -->|"proxy /api → :8080"| GWC
    GWC --> EU
    AS & AD & SS & TS & CS & NS --> EU
    AS & AD & SS & TS & CS & NS --> PG
    AD -->|"SMTP TLS"| SES2
    AD -.->|"dev fallback"| MH

    classDef docker fill:#e0f2fe,stroke:#0284c7
    classDef host fill:#fce7f3,stroke:#db2777
    classDef cloud fill:#f3f4f6,stroke:#6b7280
    class PG,MH,EU,AS,AD,SS,TS,CS,NS,GWC docker
    class FE host
    class SES2 cloud
```

---

## 5. GCP production target architecture (future)

```mermaid
flowchart TB
    Users["Users"] --> DNS["Cloud DNS<br/>app.yourdomain.com<br/>api.yourdomain.com"]

    DNS --> LB["HTTPS Load Balancer<br/>+ Managed TLS Certificate"]

    LB --> Firebase["Firebase Hosting<br/>React static build"]
    LB --> GKE["GKE Cluster<br/>(private nodes)"]

    subgraph GKE["GKE — namespace: sms"]
        Ingress["Ingress<br/>api.yourdomain.com"]
        GW3["api-gateway"]
        EU2["eureka-server"]
        MS["auth · admin · student<br/>teacher · course · notification"]
        Ingress --> GW3
        GW3 --> MS
        MS --> EU2
    end

    LB --> Ingress

    MS --> CloudSQL["Cloud SQL PostgreSQL 16<br/>6 databases · private IP"]
    MS --> SecretMgr["Secret Manager<br/>JWT · DB password · encryption key"]
    AD2["admin-service"] --> SES3["AWS SES SMTP<br/>(multi-cloud email)"]

    GKE --> CloudBuild["Cloud Build CI/CD"]
    CloudBuild --> ArtifactReg["Artifact Registry<br/>Docker images"]
    GKE --> Monitoring["Cloud Monitoring<br/>+ Cloud Logging"]

    classDef gcp fill:#e8f5e9,stroke:#34a853,color:#1b5e20
    classDef external fill:#f3f4f6,stroke:#6b7280
    class Firebase,GKE,Ingress,GW3,EU2,MS,CloudSQL,SecretMgr,CloudBuild,ArtifactReg,Monitoring,LB,DNS gcp
    class SES3 external
```

---

## 6. Database-per-service model

```mermaid
erDiagram
    AUTH_DB_USERS {
        bigint id PK
        string email UK
        string password
        string role
        boolean enabled
        int failed_login_attempts
    }

    AUTH_DB_INVITATIONS {
        bigint id PK
        string email UK
        string code
        string role
        bigint profile_id
        boolean used
        datetime expires_at
    }

    STUDENT_DB_STUDENTS {
        bigint id PK
        string email UK
        string name
        string phone "encrypted"
        string address "encrypted"
        string status
    }

    TEACHER_DB_TEACHERS {
        bigint id PK
        string email UK
        string name
        string phone "encrypted"
        string address "encrypted"
        string status
    }

    ADMIN_DB_ADMINS {
        bigint id PK
        string email UK
        string name
        string status
    }

    COURSE_DB_COURSES {
        bigint id PK
        string course_code
        string title
    }

    NOTIFICATION_DB_NOTIFICATIONS {
        bigint id PK
        string title
        string message
        string audience
    }

    AUTH_DB_INVITATIONS ||--o| AUTH_DB_USERS : "completes registration"
    AUTH_DB_INVITATIONS ||--o| STUDENT_DB_STUDENTS : "profile_id"
    AUTH_DB_INVITATIONS ||--o| TEACHER_DB_TEACHERS : "profile_id"
    AUTH_DB_INVITATIONS ||--o| ADMIN_DB_ADMINS : "profile_id"
```

---

## 7. API Gateway routing

```mermaid
flowchart LR
    REQ["Incoming Request<br/>/api/..."] --> CHECK{"JWT valid?"}
    CHECK -->|No + public path| PUB["auth/login<br/>auth/register/*"]
    CHECK -->|No| DENY1["401 Unauthorized"]
    CHECK -->|Yes| ROLE{"Role allowed<br/>for path?"}
    ROLE -->|No| DENY2["403 Forbidden"]
    ROLE -->|Yes| ROUTE{"Path prefix?"}

    ROUTE -->|/api/auth/**| AUTH3["auth-service :8081"]
    ROUTE -->|/api/admin/**| ADMIN3["admin-service :8082<br/>ADMIN only"]
    ROUTE -->|/api/students/**| STUDENT3["student-service :8083<br/>ADMIN or STUDENT"]
    ROUTE -->|/api/teachers/**| TEACHER3["teacher-service :8084<br/>ADMIN or TEACHER"]
    ROUTE -->|/api/courses/**| COURSE3["course-service :8085"]
    ROUTE -->|/api/notifications/**| NOTIF3["notification-service :8086"]
```

---

## Legend

| Symbol / term | Meaning |
|---------------|---------|
| Solid arrow | HTTP REST (via Gateway or Feign) |
| Dashed arrow | Eureka registration / optional path |
| `PENDING_REGISTRATION` | Profile created, awaiting user signup |
| `ACTIVE` | User completed registration |
| `common-lib` | Shared DTOs, security utilities, validation |
| AWS SES sandbox | Sender + recipient must be verified in SES |

---

## How to export for your report

1. Open [mermaid.live](https://mermaid.live)
2. Paste any diagram block above (without the ` ```mermaid ` fences)
3. Click **Export** → PNG or SVG
4. Add figure caption: *Figure X: SMS Microservices Architecture*

For PowerPoint / Word: export as **SVG** (scales cleanly) or **PNG** at 2× resolution.

---

*Last updated: June 2026*
