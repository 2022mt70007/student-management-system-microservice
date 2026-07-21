# Comparative Testing — VM Parity Checklist

Both deployments run on **separate VMs** in project `veritascampus-2026` (Mumbai).

| | Microservices | Monolith |
|---|---------------|----------|
| VM | `veritascampus-ms-vm` | `veritascampus-mono-vm` |
| IP | 34.93.19.119 | 8.231.106.112 |
| Domain | https://veritascampus.me | https://veritascampus.page |
| Machine | e2-standard-2 | e2-standard-2 |
| Code path | `~/NewProject` | `~/sms-monolith` |
| Start | `sudo docker compose up -d` | `sudo docker compose -f docker-compose.prod.yml up -d` |
| DB | PostgreSQL (per service) | MySQL (single) |

## Shared functionality (use for comparative tests)

Both systems support:

- Auth: login, invitation registration, JWT
- Admin: students, teachers, admins, notifications
- Academic: departments, classes, subjects
- Student/teacher dashboards
- Same seeded academic data (CS, ECE, 5 subjects)

## MS-only (exclude from parity tests)

- `/api/admin/courses`, `/api/courses/**`
- `/api/students/courses/{id}/assignments|exams`
- Student dashboard `teacherName` enrichment (optional field)

## Known behavioral differences (document in report)

| Area | Monolith | Microservices |
|------|----------|---------------|
| Internal APIs `/internal/**` | Blocked at JWT filter | Allowed through gateway with valid role |
| Profile activation | PATCH; may swallow errors | POST; fails registration on error |
| Architecture | Single process | Gateway + Eureka + Feign |

## Before each test session

1. Start VMs:
   ```bash
   gcloud compute instances start veritascampus-ms-vm veritascampus-mono-vm --zone=asia-south1-a
   ```
2. Start Docker on each VM (commands above).
3. Verify domains return 200.
4. Log in as admin on **both** apps.
5. Ensure same test users exist on both (create via admin if missing).

## Recommended test users (create on both)

| Role | Suggested email | Notes |
|------|-----------------|-------|
| Admin | sidharthsangamam@gmail.com | Already ACTIVE on both |
| Student | ajai3237wk1@gmail.com | Exists on MS; create on monolith if missing |
| Teacher | (create one) | For RBAC tests |

## Verify parity on VM

```bash
bash verify-parity.sh ms    # on veritascampus-ms-vm
bash verify-parity.sh mono  # on veritascampus-mono-vm
```

Expected academic counts: **depts=2, classes=2, subjects=5**
